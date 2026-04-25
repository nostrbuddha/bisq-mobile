package network.bisq.mobile.presentation.trade.trade_detail.states

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
import network.bisq.mobile.data.replicated.presentation.open_trades.TradeItemPresentationModel
import network.bisq.mobile.data.service.accounts.UserDefinedAccountsServiceFacade
import network.bisq.mobile.data.service.explorer.ExplorerServiceFacade
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.domain.model.account.fiat.UserDefinedFiatAccount
import network.bisq.mobile.domain.model.account.fiat.UserDefinedFiatAccountPayload
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.test_utils.startKoinForPresenterUnitTests
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.main.MainPresenter
import network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_1.state_a.BuyerState1aPresenter
import network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_2.state_a.BuyerState2aPresenter
import network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_3.state_b.BuyerStateLightning3bPresenter
import network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_3.state_b.BuyerStateMainChain3bPresenter
import network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_1.SellerState1Presenter
import network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_2.state_b.SellerState2bPresenter
import network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_a.SellerState3aPresenter
import network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_b.SellerStateLightning3bPresenter
import network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_3.state_b.SellerStateMainChain3bPresenter
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TradeDetailStatePresentersTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var globalUi: GlobalUiManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        I18nSupport.initialize("en")
        globalUi = mockk(relaxed = true)
        startKoinForPresenterUnitTests(globalUi)
    }

    @AfterTest
    fun tearDown() {
        try {
            stopKoin()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `SellerState1 onSendPaymentData shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.sellerSendsPaymentAccount(any()) } returns Result.success(Unit)
            val account =
                UserDefinedFiatAccount(
                    accountName = "A",
                    accountPayload = UserDefinedFiatAccountPayload(accountData = "data@x.com"),
                )
            val accounts = mockk<UserDefinedAccountsServiceFacade>(relaxed = true)
            coEvery { accounts.getAccounts() } returns Result.success(listOf(account))

            val presenter = SellerState1Presenter(main, trades, accounts)
            presenter.onViewAttached()
            advanceUntilIdle()
            presenter.onPaymentDataInput("data@x.com")
            presenter.setPaymentAccountName("Acc")
            presenter.onSendPaymentData()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isSendPaymentDataLoading.value)
        }

    @Test
    fun `BuyerState1a sendBitcoinPaymentData shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.buyerSendBitcoinPaymentData(any()) } returns Result.success(Unit)
            val trade = mockk<TradeItemPresentationModel>(relaxed = true)
            every { trades.selectedTrade } returns MutableStateFlow(trade)

            val presenter = BuyerState1aPresenter(main, trades)
            presenter.onViewAttached()
            presenter.onBitcoinPaymentDataInput("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", true)
            presenter.sendBitcoinPaymentData()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isSendBitcoinPaymentDataLoading.value)
        }

    @Test
    fun `BuyerState2a onConfirmFiatSent shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.buyerConfirmFiatSent() } returns Result.success(Unit)
            val presenter = BuyerState2aPresenter(main, trades)
            presenter.onConfirmFiatSent()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isConfirmFiatSentLoading.value)
        }

    @Test
    fun `SellerState2b onConfirmFiatReceipt shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.sellerConfirmFiatReceipt() } returns Result.success(Unit)
            val presenter = SellerState2bPresenter(main, trades)
            presenter.onConfirmFiatReceipt()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isConfirmFiatReceiptLoading.value)
        }

    @Test
    fun `SellerState3a confirmSend shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.sellerConfirmBtcSent(any()) } returns Result.success(Unit)
            val trade = mockk<TradeItemPresentationModel>(relaxed = true)
            every { trades.selectedTrade } returns MutableStateFlow(trade)
            val presenter = SellerState3aPresenter(main, trades)
            presenter.onViewAttached()
            presenter.onPaymentProofInput("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", true)
            presenter.confirmSend()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isConfirmBtcSentLoading.value)
        }

    @Test
    fun `BuyerStateLightning3b onCompleteTrade shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.btcConfirmed() } returns Result.success(Unit)
            val presenter = BuyerStateLightning3bPresenter(main, trades)
            presenter.onCompleteTrade()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isCompleteTradeLoading.value)
        }

    @Test
    fun `SellerStateLightning3b skipWaiting shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.btcConfirmed() } returns Result.success(Unit)
            val trade = mockk<TradeItemPresentationModel>(relaxed = true)
            every { trades.selectedTrade } returns MutableStateFlow(trade)
            val presenter = SellerStateLightning3bPresenter(main, trades)
            presenter.onViewAttached()
            advanceUntilIdle()
            presenter.skipWaiting()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isBtcConfirmedLoading.value)
        }

    @Test
    fun `BuyerStateMainChain3b onCompleteTrade shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.btcConfirmed() } returns Result.success(Unit)
            every { trades.selectedTrade } returns MutableStateFlow(mockk(relaxed = true))
            val explorer = mockk<ExplorerServiceFacade>(relaxed = true)
            val presenter = BuyerStateMainChain3bPresenter(main, trades, explorer)
            presenter.onCompleteTrade()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isCompleteTradeLoading.value)
        }

    @Test
    fun `BuyerStateMainChain3b onCtaClick completes trade same as onCompleteTrade when no amount mismatch`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.btcConfirmed() } returns Result.success(Unit)
            every { trades.selectedTrade } returns MutableStateFlow(mockk(relaxed = true))
            val explorer = mockk<ExplorerServiceFacade>(relaxed = true)
            val presenter = BuyerStateMainChain3bPresenter(main, trades, explorer)
            presenter.onCtaClick()
            advanceUntilIdle()

            coVerify(exactly = 1) { trades.btcConfirmed() }
            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isCompleteTradeLoading.value)
        }

    @Test
    fun `BuyerStateMainChain3b onCompleteTrade keeps isCompleteTradeLoading true while btcConfirmed runs`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            every { trades.selectedTrade } returns MutableStateFlow(mockk(relaxed = true))
            val explorer = mockk<ExplorerServiceFacade>(relaxed = true)
            val presenter = BuyerStateMainChain3bPresenter(main, trades, explorer)
            coEvery { trades.btcConfirmed() } coAnswers {
                assertTrue(presenter.isCompleteTradeLoading.value)
                Result.success(Unit)
            }
            presenter.onCompleteTrade()
            advanceUntilIdle()
            assertFalse(presenter.isCompleteTradeLoading.value)
        }

    @Test
    fun `BuyerStateMainChain3b onCompleteTrade clears loading when btcConfirmed fails`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.btcConfirmed() } returns Result.failure(RuntimeException("btcConfirmed failed"))
            every { trades.selectedTrade } returns MutableStateFlow(mockk(relaxed = true))
            val explorer = mockk<ExplorerServiceFacade>(relaxed = true)
            val presenter = BuyerStateMainChain3bPresenter(main, trades, explorer)
            presenter.onCompleteTrade()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isCompleteTradeLoading.value)
        }

    @Test
    fun `SellerStateMainChain3b onCompleteTrade shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.btcConfirmed() } returns Result.success(Unit)
            every { trades.selectedTrade } returns MutableStateFlow(mockk(relaxed = true))
            val explorer = mockk<ExplorerServiceFacade>(relaxed = true)
            val presenter = SellerStateMainChain3bPresenter(main, trades, explorer)
            presenter.onCompleteTrade()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isCompleteTradeLoading.value)
        }

    @Test
    fun `SellerStateMainChain3b onCtaClick completes trade same as onCompleteTrade when no amount mismatch`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.btcConfirmed() } returns Result.success(Unit)
            every { trades.selectedTrade } returns MutableStateFlow(mockk(relaxed = true))
            val explorer = mockk<ExplorerServiceFacade>(relaxed = true)
            val presenter = SellerStateMainChain3bPresenter(main, trades, explorer)
            presenter.onCtaClick()
            advanceUntilIdle()

            coVerify(exactly = 1) { trades.btcConfirmed() }
            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isCompleteTradeLoading.value)
        }

    @Test
    fun `SellerStateMainChain3b onCompleteTrade clears loading when btcConfirmed fails`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            coEvery { trades.btcConfirmed() } returns Result.failure(RuntimeException("btcConfirmed failed"))
            every { trades.selectedTrade } returns MutableStateFlow(mockk(relaxed = true))
            val explorer = mockk<ExplorerServiceFacade>(relaxed = true)
            val presenter = SellerStateMainChain3bPresenter(main, trades, explorer)
            presenter.onCompleteTrade()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isCompleteTradeLoading.value)
        }

    @Test
    fun `SellerStateMainChain3b onCompleteTrade keeps isCompleteTradeLoading true while btcConfirmed runs`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val trades = mockk<TradesServiceFacade>(relaxed = true)
            every { trades.selectedTrade } returns MutableStateFlow(mockk(relaxed = true))
            val explorer = mockk<ExplorerServiceFacade>(relaxed = true)
            val presenter = SellerStateMainChain3bPresenter(main, trades, explorer)
            coEvery { trades.btcConfirmed() } coAnswers {
                assertTrue(presenter.isCompleteTradeLoading.value)
                Result.success(Unit)
            }
            presenter.onCompleteTrade()
            advanceUntilIdle()
            assertFalse(presenter.isCompleteTradeLoading.value)
        }
}
