package network.bisq.mobile.presentation.ui.uicases.startup

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.bisq.mobile.client.websocket.WebSocketClientProvider
import network.bisq.mobile.domain.data.model.Settings
import network.bisq.mobile.domain.data.repository.SettingsRepository
import network.bisq.mobile.presentation.BasePresenter
import network.bisq.mobile.presentation.MainPresenter
import network.bisq.mobile.presentation.ui.navigation.Routes

class TrustedNodeSetupPresenter(
    mainPresenter: MainPresenter,
    private val settingsRepository: SettingsRepository,
    private val webSocketClientProvider: WebSocketClientProvider
) : BasePresenter(mainPresenter), ITrustedNodeSetupPresenter {

    private val _isBisqApiUrlValid = MutableStateFlow(false)
    override val isBisqApiUrlValid: StateFlow<Boolean> = _isBisqApiUrlValid

    private val _bisqApiUrl = MutableStateFlow("ws://10.0.2.2:8090")
    override val bisqApiUrl: StateFlow<String> = _bisqApiUrl

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    override fun onViewAttached() {
        super.onViewAttached()
        initialize()
    }

    private fun initialize() {
        log.i { "View attached to Trusted node presenter"}
        backgroundScope.launch {
            try {
                settingsRepository.fetch()
                settingsRepository.data.value.let {
                    it?.let {
                        log.d { "Settings url:${it.bisqApiUrl}" }
                        updateBisqApiUrl(it.bisqApiUrl, true)
                    }
                }
            } catch (e: Exception) {
                log.e("Failed to load from repository", e)
            }
        }
    }

    override fun updateBisqApiUrl(newUrl: String, isValid: Boolean) {
        // log.w { "$newUrl: $isValid" }
        _isBisqApiUrlValid.value = isValid
        _bisqApiUrl.value = newUrl
        _isConnected.value = false
    }

    override fun validateWsUrl(url: String): String? {
        val wsUrlPattern =
            """^(ws|wss):\/\/(([a-zA-Z0-9.-]+\.[a-zA-Z]{2,}|localhost)|(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}))(:\d{1,5})$""".toRegex()
        if (url.isEmpty()) {
            return "URL cannot be empty" //TODO:i18n
        }
        if (!wsUrlPattern.matches(url)) {
            return "Invalid WebSocket URL. Must be ws:// or wss:// followed by a domain/IP and port" //TODO:i18n
        }
        return null
    }

    override fun testConnection(isWorkflow: Boolean) {
        backgroundScope.launch {
            _isLoading.value = true
            log.w { "Test: " + _bisqApiUrl.value }
            WebSocketClientProvider.parseUri(_bisqApiUrl.value).let { connectionSettings ->
                if (webSocketClientProvider.testClient(connectionSettings.first, connectionSettings.second)) {
                    updateTrustedNodeSettings()
                    _isConnected.value = true
                    showSnackbar("Connected successfully to ${_bisqApiUrl.value}, settings updated")
                    if (!isWorkflow) {
                        navigateBack();
                    }
                } else {
                    showSnackbar("Could not connect to given url ${_bisqApiUrl.value}, please try again with another setup")
                    _isConnected.value = false
                }
                _isLoading.value = false
            }
        }
    }

    private suspend fun updateTrustedNodeSettings() {
        val currentSettings = settingsRepository.fetch()
        val updatedSettings = Settings().apply {
            bisqApiUrl = _bisqApiUrl.value
            firstLaunch = currentSettings?.firstLaunch ?: true
        }
        settingsRepository.update(updatedSettings)
    }

    override fun navigateToNextScreen() {
        navigateTo(Routes.CreateProfile)
    }

    override fun goBackToSetupScreen() {
        navigateBack()
    }
}
