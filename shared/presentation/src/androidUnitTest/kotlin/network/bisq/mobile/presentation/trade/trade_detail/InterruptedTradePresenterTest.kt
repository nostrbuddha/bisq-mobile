package network.bisq.mobile.presentation.trade.open_trade

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
import network.bisq.mobile.data.service.mediation.MediationServiceFacade
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.domain.repository.TradeReadStateRepository
import network.bisq.mobile.domain.utils.CoroutineExceptionHandlerSetup
import network.bisq.mobile.domain.utils.CoroutineJobsManager
import network.bisq.mobile.domain.utils.DefaultCoroutineJobsManager
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.common.ui.error.GenericErrorHandler
import network.bisq.mobile.presentation.common.ui.navigation.manager.NavigationManager
import network.bisq.mobile.presentation.main.MainPresenter
import network.bisq.mobile.presentation.trade.trade_detail.InterruptedTradePresenter
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class InterruptedTradePresenterTest {
    private val testDispatcher = StandardTestDispatcher()

    private val mainPresenter: MainPresenter = mockk(relaxed = true)
    private val tradesServiceFacade: TradesServiceFacade = mockk(relaxed = true)
    private val mediationServiceFacade: MediationServiceFacade = mockk(relaxed = true)
    private val tradeReadStateRepository: TradeReadStateRepository = mockk(relaxed = true)
    private val navigationManager: NavigationManager = mockk(relaxed = true)
    private lateinit var globalUiManager: GlobalUiManager

    private val testKoinModule =
        module {
            single { CoroutineExceptionHandlerSetup() }
            factory<CoroutineJobsManager> {
                DefaultCoroutineJobsManager().apply {
                    get<CoroutineExceptionHandlerSetup>().setupExceptionHandler(this)
                }
            }
            single<NavigationManager> { navigationManager }
            single<GlobalUiManager> { globalUiManager }
        }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        globalUiManager = mockk(relaxed = true)
        startKoin { modules(testKoinModule) }
        I18nSupport.initialize("en")
        GenericErrorHandler.clearGenericError()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        GenericErrorHandler.clearGenericError()
    }

    @Test
    fun onCloseTrade_success_clearsReadState_navigatesBack_and_hidesLoading() =
        runTest(testDispatcher) {
            val tradeItem = mockk<TradeItemPresentationModel>(relaxed = true)
            every { tradeItem.tradeId } returns "t-1"
            val selectedFlow = MutableStateFlow<TradeItemPresentationModel?>(tradeItem)
            every { tradesServiceFacade.selectedTrade } returns selectedFlow
            coEvery { tradesServiceFacade.closeTrade() } returns Result.success(Unit)

            val presenter =
                InterruptedTradePresenter(
                    mainPresenter,
                    tradesServiceFacade,
                    mediationServiceFacade,
                    tradeReadStateRepository,
                )

            presenter.onCloseTrade()
            advanceUntilIdle()

            coVerify(timeout = 5000) { tradesServiceFacade.closeTrade() }
            coVerify(timeout = 5000) { tradeReadStateRepository.clearId("t-1") }
            advanceUntilIdle()
            verify { navigationManager.navigateBack(any()) }
            assertFalse(presenter.isProcessing.value)
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
        }

    @Test
    fun onCloseTrade_failure_showsError_doesNotNavigate_and_hidesLoading() =
        runTest(testDispatcher) {
            val tradeItem = mockk<TradeItemPresentationModel>(relaxed = true)
            every { tradeItem.tradeId } returns "t-2"
            val selectedFlow = MutableStateFlow<TradeItemPresentationModel?>(tradeItem)
            every { tradesServiceFacade.selectedTrade } returns selectedFlow
            coEvery { tradesServiceFacade.closeTrade() } returns Result.failure(RuntimeException("boom"))

            val presenter =
                InterruptedTradePresenter(
                    mainPresenter,
                    tradesServiceFacade,
                    mediationServiceFacade,
                    tradeReadStateRepository,
                )

            presenter.onCloseTrade()
            advanceUntilIdle()

            coVerify { tradesServiceFacade.closeTrade() }
            coVerify(exactly = 0) { tradeReadStateRepository.clearId(any()) }
            verify(exactly = 0) { navigationManager.navigateBack(any()) }
            assertFalse(presenter.isProcessing.value)
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
            assertEquals(
                "mobile.bisqEasy.openTrades.closeTrade.failed".i18n("boom"),
                GenericErrorHandler.genericErrorMessage.value,
            )
        }

    @Test
    fun onCloseTrade_success_but_clearReadState_throws_showsError_and_still_navigates() =
        runTest(testDispatcher) {
            val tradeItem = mockk<TradeItemPresentationModel>(relaxed = true)
            every { tradeItem.tradeId } returns "t-3"
            val selectedFlow = MutableStateFlow<TradeItemPresentationModel?>(tradeItem)
            every { tradesServiceFacade.selectedTrade } returns selectedFlow
            coEvery { tradesServiceFacade.closeTrade() } returns Result.success(Unit)
            coEvery { tradeReadStateRepository.clearId("t-3") } throws IllegalStateException("fail-clear")

            val presenter =
                InterruptedTradePresenter(
                    mainPresenter,
                    tradesServiceFacade,
                    mediationServiceFacade,
                    tradeReadStateRepository,
                )

            presenter.onCloseTrade()
            advanceUntilIdle()

            coVerify(timeout = 5000) { tradeReadStateRepository.clearId("t-3") }
            advanceUntilIdle()
            verify { navigationManager.navigateBack(any()) }
            assertFalse(presenter.isProcessing.value)
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
            assertEquals(
                "mobile.bisqEasy.openTrades.clearReadState.failed".i18n("fail-clear"),
                GenericErrorHandler.genericErrorMessage.value,
            )
        }
}
