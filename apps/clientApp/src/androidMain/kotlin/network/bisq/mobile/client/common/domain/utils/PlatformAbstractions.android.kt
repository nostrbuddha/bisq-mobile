package network.bisq.mobile.client.common.domain.utils

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.Dispatchers
import network.bisq.mobile.client.common.domain.access.security.TlsTrustManager
import network.bisq.mobile.client.common.domain.httpclient.BisqProxyConfig
import network.bisq.mobile.data.utils.NoDns
import network.bisq.mobile.domain.utils.getLogger
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

/**
 * Set to true to emit WebSocket lifecycle events to logcat.
 * Filter with: adb logcat -s OkHttpSocket
 *
 * Background: OkHttp 5.x's RealWebSocket.connect() hardcodes EventListener.NONE for every
 * WebSocket connection, so an eventListenerFactory set on the OkHttpClient is silently
 * suppressed for the /websocket path. The WebSocket.Factory wrapper below intercepts at the
 * WebSocketListener level instead, which is the only hook that survives that override.
 * Network interceptors still fire for plain HTTP calls (e.g. /api/v1/access/session), which
 * is why those appeared in the logs but /websocket events did not.
 */
private const val SOCKET_LOGGING_ENABLED = true

actual fun createHttpClient(
    host: String,
    tlsFingerprint: String?,
    proxyConfig: BisqProxyConfig?,
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient {
    // Build the OkHttpClient explicitly so we can close over it inside webSocketFactory.
    // Ktor's engine { preconfigured = ... } uses this as the base for newBuilder(), then
    // overlays its own followRedirects/retryOnConnectionFailure defaults and a fresh Dispatcher.
    val baseClient = OkHttpClient.Builder().apply {
        proxyConfig?.config?.let { proxy(it) }
        if (proxyConfig?.isTorProxy == true) {
            dns(NoDns())
            // Mirror the HttpTimeout values set in HttpClientService.createNewInstance()
            // for Tor connections. The default OkHttp connect timeout (10 s) is far too
            // short for v3 onion circuit establishment, which regularly takes 15–60 s.
            connectTimeout(120, TimeUnit.SECONDS)
            readTimeout(120, TimeUnit.SECONDS)
            writeTimeout(120, TimeUnit.SECONDS)
        }
        pingInterval(15, TimeUnit.SECONDS)

        tlsFingerprint?.let {
            try {
                val tlsTrustManager = TlsTrustManager(host, tlsFingerprint)
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, arrayOf(tlsTrustManager), SecureRandom())
                sslSocketFactory(sslContext.socketFactory, tlsTrustManager)
                // We verify host in the TrustManager, thus we can return always true in hostnameVerifier
                hostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                getLogger("").e { "Error applying SSLContext $tlsFingerprint" }
                throw e
            }
        }
    }.build()

    return HttpClient(OkHttp) {
        config(this)
        install(WebSockets)
        engine {
            dispatcher = Dispatchers.IO
            proxy = proxyConfig?.config
            preconfigured = baseClient

            if (SOCKET_LOGGING_ENABLED) {
                // webSocketFactory intercepts every OkHttpClient.newWebSocket() call made by
                // Ktor's OkHttpWebsocketSession. We wrap the listener Ktor passes in so we
                // observe onOpen / onFailure / onClosed without touching the call path.
                webSocketFactory = WebSocket.Factory { request, listener ->
                    val path = request.url.encodedPath
                    Log.d("OkHttpSocket", "ws newWebSocket  $path")
                    baseClient.newWebSocket(
                        request,
                        object : WebSocketListener() {
                            override fun onOpen(webSocket: WebSocket, response: Response) {
                                Log.d("OkHttpSocket", "ws onOpen  $path  code=${response.code}")
                                listener.onOpen(webSocket, response)
                            }

                            override fun onFailure(
                                webSocket: WebSocket,
                                t: Throwable,
                                response: Response?,
                            ) {
                                Log.d(
                                    "OkHttpSocket",
                                    "ws onFailure  $path  err=${t.message}",
                                )
                                listener.onFailure(webSocket, t, response)
                            }

                            override fun onClosing(
                                webSocket: WebSocket,
                                code: Int,
                                reason: String,
                            ) {
                                Log.d("OkHttpSocket", "ws onClosing  $path  code=$code")
                                listener.onClosing(webSocket, code, reason)
                            }

                            override fun onClosed(
                                webSocket: WebSocket,
                                code: Int,
                                reason: String,
                            ) {
                                Log.d("OkHttpSocket", "ws onClosed  $path  code=$code")
                                listener.onClosed(webSocket, code, reason)
                            }

                            override fun onMessage(webSocket: WebSocket, text: String) {
                                listener.onMessage(webSocket, text)
                            }

                            override fun onMessage(
                                webSocket: WebSocket,
                                bytes: okio.ByteString,
                            ) {
                                listener.onMessage(webSocket, bytes)
                            }
                        },
                    )
                }
            }
        }
    }
}
