package network.bisq.mobile.presentation.trade.trade_chat

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.service.chat.trade.TradeChatMessagesServiceFacade
import network.bisq.mobile.data.service.message_delivery.MessageDeliveryServiceFacade
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.domain.repository.SettingsRepository
import network.bisq.mobile.domain.repository.TradeReadStateRepository
import network.bisq.mobile.domain.utils.CoroutineJobsManager
import network.bisq.mobile.domain.utils.DefaultCoroutineJobsManager
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.notification.NotificationController
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.common.ui.navigation.manager.NavigationManager
import network.bisq.mobile.presentation.main.MainPresenter
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TradeChatPresenterLoadingTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var globalUiManager: GlobalUiManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        I18nSupport.initialize("en")
        globalUiManager = mockk(relaxed = true)
        startKoin {
            modules(
                module {
                    single<NavigationManager> { mockk(relaxed = true) }
                    single<CoroutineJobsManager> { DefaultCoroutineJobsManager() }
                    single<GlobalUiManager> { globalUiManager }
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        try {
            stopKoin()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun buildPresenter(
        mainPresenter: MainPresenter,
        tradeChat: TradeChatMessagesServiceFacade,
        userProfileServiceFacade: UserProfileServiceFacade,
    ) = TradeChatPresenter(
        mainPresenter = mainPresenter,
        tradesServiceFacade = mockk<TradesServiceFacade>(relaxed = true),
        tradeChatMessagesServiceFacade = tradeChat,
        settingsRepository = mockk<SettingsRepository>(relaxed = true),
        tradeReadStateRepository = mockk<TradeReadStateRepository>(relaxed = true),
        userProfileServiceFacade = userProfileServiceFacade,
        notificationController = mockk<NotificationController>(relaxed = true),
        messageDeliveryServiceFacade = mockk<MessageDeliveryServiceFacade>(relaxed = true),
    )

    @Test
    fun `sendChatMessage shows loading inFlight then clears`() =
        runTest(testDispatcher) {
            val tradeChat = mockk<TradeChatMessagesServiceFacade>(relaxed = true)
            coEvery { tradeChat.sendChatMessage(any(), any()) } returns Result.success(Unit)
            val mainPresenter = mockk<MainPresenter>(relaxed = true)
            val userProfileServiceFacade = mockk<UserProfileServiceFacade>(relaxed = true)
            every { userProfileServiceFacade.ignoredProfileIds } returns MutableStateFlow(emptySet())
            val presenter = buildPresenter(mainPresenter, tradeChat, userProfileServiceFacade)

            presenter.sendChatMessage("hello")
            assertTrue(presenter.isSendingChatMessage.value)
            advanceUntilIdle()

            assertFalse(presenter.isSendingChatMessage.value)
            coVerify { tradeChat.sendChatMessage("hello", null) }
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
        }

    @Test
    fun `onConfirmedUndoIgnoreUser sets inFlight and show hide loading`() =
        runTest(testDispatcher) {
            val userProfileServiceFacade = mockk<UserProfileServiceFacade>(relaxed = true)
            val peer = createMockUserProfile("peer")
            every { userProfileServiceFacade.ignoredProfileIds } returns
                MutableStateFlow(setOf(peer.networkId.pubKey.id))
            coEvery { userProfileServiceFacade.undoIgnoreUserProfile(any()) } returns Unit
            val mainPresenter = mockk<MainPresenter>(relaxed = true)
            val presenter =
                buildPresenter(
                    mainPresenter,
                    mockk<TradeChatMessagesServiceFacade>(relaxed = true),
                    userProfileServiceFacade,
                )

            presenter.onConfirmedUndoIgnoreUser(peer.networkId.pubKey.id)
            assertTrue(presenter.isUndoIgnoreInFlight.value)
            advanceUntilIdle()

            assertFalse(presenter.isUndoIgnoreInFlight.value)
            coVerify { userProfileServiceFacade.undoIgnoreUserProfile(peer.networkId.pubKey.id) }
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
        }

    @Test
    fun `onConfirmedIgnoreUser schedules show and hide loading`() =
        runTest(testDispatcher) {
            val userProfileServiceFacade = mockk<UserProfileServiceFacade>(relaxed = true)
            every { userProfileServiceFacade.ignoredProfileIds } returns MutableStateFlow(emptySet())
            coEvery { userProfileServiceFacade.ignoreUserProfile(any()) } returns Unit
            val mainPresenter = mockk<MainPresenter>(relaxed = true)
            val presenter =
                buildPresenter(
                    mainPresenter,
                    mockk<TradeChatMessagesServiceFacade>(relaxed = true),
                    userProfileServiceFacade,
                )

            presenter.onConfirmedIgnoreUser("ign-id")
            advanceUntilIdle()

            coVerify { userProfileServiceFacade.ignoreUserProfile("ign-id") }
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
        }
}
