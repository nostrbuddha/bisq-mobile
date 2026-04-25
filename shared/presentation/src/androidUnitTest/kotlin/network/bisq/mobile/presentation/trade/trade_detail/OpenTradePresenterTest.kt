package network.bisq.mobile.presentation.trade.trade_detail

import androidx.compose.foundation.ScrollState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import network.bisq.mobile.data.model.TradeReadStateMap
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessageModel
import network.bisq.mobile.data.replicated.presentation.open_trades.TradeItemPresentationModel
import network.bisq.mobile.data.replicated.trade.bisq_easy.protocol.BisqEasyTradeStateEnum
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.domain.repository.TradeReadStateRepository
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.test_utils.startKoinForPresenterUnitTests
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.common.ui.error.GenericErrorHandler
import network.bisq.mobile.presentation.main.MainPresenter
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class OpenTradePresenterTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var globalUi: GlobalUiManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        I18nSupport.initialize("en")
        GenericErrorHandler.clearGenericError()
        globalUi = mockk(relaxed = true)
        startKoinForPresenterUnitTests(globalUi)
    }

    @AfterTest
    fun tearDown() {
        try {
            stopKoin()
        } finally {
            GenericErrorHandler.clearGenericError()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `onConfirmedUndoIgnoreUser shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val peer = createMockUserProfile("peer")
            val trade = mockk<TradeItemPresentationModel>(relaxed = true)
            every { trade.tradeId } returns "trade-1"
            every { trade.peersUserProfile } returns peer
            every { trade.bisqEasyTradeModel.tradeState } returns
                MutableStateFlow(BisqEasyTradeStateEnum.BTC_CONFIRMED)
            every { trade.bisqEasyOpenTradeChannelModel.chatMessages } returns
                MutableStateFlow<Set<BisqEasyOpenTradeMessageModel>>(emptySet())
            every { trade.bisqEasyOpenTradeChannelModel.isInMediation } returns MutableStateFlow(false)

            val selected = MutableStateFlow<TradeItemPresentationModel?>(trade)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            every { trades.selectedTrade } returns selected
            coEvery { trades.selectOpenTrade(any()) } coAnswers { }

            val users = mockk<UserProfileServiceFacade>(relaxed = true)
            coEvery { users.undoIgnoreUserProfile("id") } returns Unit
            every { users.ignoredProfileIds } returns MutableStateFlow(emptySet())

            val tradeRead = mockk<TradeReadStateRepository>(relaxed = true)
            every { tradeRead.data } returns flowOf(TradeReadStateMap())

            val tradeFlow = mockk<TradeFlowPresenter>(relaxed = true)

            val presenter =
                OpenTradePresenter(
                    main,
                    tradeRead,
                    trades,
                    users,
                    tradeFlow,
                )

            val uiScope = CoroutineScope(testDispatcher + SupervisorJob())
            val scrollState = mockk<ScrollState>(relaxed = true)
            presenter.initialize("trade-1", scrollState, uiScope)
            advanceUntilIdle()

            presenter.onConfirmedUndoIgnoreUser()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isUndoIgnoreInFlight.value)
        }
}
