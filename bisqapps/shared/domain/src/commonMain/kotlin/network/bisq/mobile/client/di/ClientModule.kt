package network.bisq.mobile.client.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import network.bisq.mobile.android.node.main.bootstrap.ClientApplicationBootstrapFacade
import network.bisq.mobile.client.service.ApiRequestService
import network.bisq.mobile.domain.client.main.user_profile.ClientUserProfileServiceFacade
import network.bisq.mobile.domain.client.main.user_profile.UserProfileApiGateway
import network.bisq.mobile.domain.data.repository.main.bootstrap.ApplicationBootstrapFacade
import network.bisq.mobile.domain.user_profile.UserProfileServiceFacade
import network.bisq.mobile.utils.ByteArrayAsBase64Serializer
import org.koin.core.qualifier.named
import org.koin.dsl.module


// networking and services dependencies
val clientModule = module {

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    serializersModule = SerializersModule {
                        contextual(ByteArrayAsBase64Serializer)
                    }
                    ignoreUnknownKeys = true
                })
            }
        }
    }


    single<ApplicationBootstrapFacade> { ClientApplicationBootstrapFacade() }

    single(named("ApiBaseUrl")) { provideApiBaseUrl() }
    single { ApiRequestService(get(), get<String>(named("ApiBaseUrl"))) }
    single { UserProfileApiGateway(get()) }
    single<UserProfileServiceFacade> { ClientUserProfileServiceFacade(get()) }
}

fun provideApiBaseUrl(): String {
    return "10.0.2.2" // Default for Android emulator
}