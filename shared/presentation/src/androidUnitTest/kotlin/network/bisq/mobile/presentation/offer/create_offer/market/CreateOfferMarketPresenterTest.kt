package network.bisq.mobile.presentation.offer.create_offer.market

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
import network.bisq.mobile.data.model.offerbook.MarketListItem
import network.bisq.mobile.data.replicated.common.currency.MarketVO
import network.bisq.mobile.data.replicated.offer.DirectionEnum
import network.bisq.mobile.data.service.market_price.MarketPriceServiceFacade
import network.bisq.mobile.data.service.offers.OffersServiceFacade
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.test_utils.startKoinForPresenterUnitTests
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.main.MainPresenter
import network.bisq.mobile.presentation.offer.create_offer.CreateOfferCoordinator
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class CreateOfferMarketPresenterTest {
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
    fun `onViewAttached initial market load shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val market = MarketVO("BTC", "USD", "Bitcoin", "US Dollar")
            val model =
                CreateOfferCoordinator.CreateOfferModel().also {
                    it.market = market
                    it.direction = DirectionEnum.BUY
                }
            val createOfferCoordinator = mockk<CreateOfferCoordinator>(relaxed = true)
            every { createOfferCoordinator.createOfferModel } returns model
            val offerItems = MutableStateFlow<List<MarketListItem>>(emptyList())
            val offers = mockk<OffersServiceFacade>(relaxed = true)
            every { offers.offerbookMarketItems } returns offerItems
            val marketPrice =
                mockk<MarketPriceServiceFacade>(relaxed = true).also {
                    every { it.globalPriceUpdate } returns MutableStateFlow(0L)
                }
            val presenter =
                CreateOfferMarketPresenter(
                    main,
                    offers,
                    createOfferCoordinator,
                    marketPrice,
                )
            presenter.onViewAttached()
            offerItems.value = listOf(MarketListItem.from(market, numOffers = 1, languageCode = "en"))
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isInitialMarketsLoad.value)
        }
}
