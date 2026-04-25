package network.bisq.mobile.presentation.offer.take_offer.review

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import network.bisq.mobile.data.model.BatteryOptimizationState
import network.bisq.mobile.data.model.PermissionState
import network.bisq.mobile.data.model.Settings
import network.bisq.mobile.data.model.TradeReadStateMap
import network.bisq.mobile.data.model.market.MarketFilter
import network.bisq.mobile.data.model.market.MarketPriceItem
import network.bisq.mobile.data.model.market.MarketSortBy
import network.bisq.mobile.data.model.offerbook.MarketListItem
import network.bisq.mobile.data.replicated.common.currency.MarketVO
import network.bisq.mobile.data.replicated.common.currency.MarketVOFactory
import network.bisq.mobile.data.replicated.common.monetary.PriceQuoteVOFactory
import network.bisq.mobile.data.replicated.common.network.AddressByTransportTypeMapVO
import network.bisq.mobile.data.replicated.network.identity.NetworkIdVO
import network.bisq.mobile.data.replicated.offer.DirectionEnum
import network.bisq.mobile.data.replicated.offer.amount.spec.QuoteSideFixedAmountSpecVO
import network.bisq.mobile.data.replicated.offer.bisq_easy.BisqEasyOfferVO
import network.bisq.mobile.data.replicated.offer.payment_method.BitcoinPaymentMethodSpecVO
import network.bisq.mobile.data.replicated.offer.payment_method.FiatPaymentMethodSpecVO
import network.bisq.mobile.data.replicated.offer.price.spec.FixPriceSpecVO
import network.bisq.mobile.data.replicated.presentation.offerbook.OfferItemPresentationDto
import network.bisq.mobile.data.replicated.presentation.offerbook.OfferItemPresentationModel
import network.bisq.mobile.data.replicated.security.keys.PubKeyVO
import network.bisq.mobile.data.replicated.security.keys.PublicKeyVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.data.service.market_price.MarketPriceServiceFacade
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.domain.repository.SettingsRepository
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.test_utils.startKoinForPresenterUnitTests
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.common.ui.platform.getScreenWidthDp
import network.bisq.mobile.presentation.main.MainPresenter
import network.bisq.mobile.presentation.offer.take_offer.TakeOfferCoordinator
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class TakeOfferReviewPresenterTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var globalUi: GlobalUiManager

    private class TestSettings : SettingsRepository {
        private val _data = MutableStateFlow(Settings())
        override val data: kotlinx.coroutines.flow.Flow<Settings> = _data

        override suspend fun setFirstLaunch(value: Boolean) {}

        override suspend fun setShowChatRulesWarnBox(value: Boolean) {}

        override suspend fun setSelectedMarketCode(value: String) {}

        override suspend fun setNotificationPermissionState(value: PermissionState) {}

        override suspend fun setBatteryOptimizationPermissionState(value: BatteryOptimizationState) {}

        override suspend fun update(transform: suspend (t: Settings) -> Settings) {
            _data.value = transform(_data.value)
        }

        override suspend fun clear() {
            _data.value = Settings()
        }

        override suspend fun setMarketSortBy(value: MarketSortBy) {}

        override suspend fun setMarketFilter(value: MarketFilter) {}

        override suspend fun setDontShowAgainHyperlinksOpenInBrowser(value: Boolean) {}

        override suspend fun setPermitOpeningBrowser(value: Boolean) {}
    }

    private class TestMarket(
        settingsRepository: SettingsRepository,
        private val prices: Map<MarketVO, MarketPriceItem>,
    ) : MarketPriceServiceFacade(settingsRepository) {
        override fun findMarketPriceItem(marketVO: MarketVO): MarketPriceItem? =
            prices.entries
                .firstOrNull { (k, _) ->
                    k.baseCurrencyCode == marketVO.baseCurrencyCode && k.quoteCurrencyCode == marketVO.quoteCurrencyCode
                }?.value

        override fun findUSDMarketPriceItem(): MarketPriceItem? = findMarketPriceItem(MarketVO("BTC", "USD"))

        override fun refreshSelectedFormattedMarketPrice() {}

        override fun selectMarket(marketListItem: MarketListItem): Result<Unit> = Result.success(Unit)
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        I18nSupport.initialize("en")
        mockkStatic("network.bisq.mobile.presentation.common.ui.platform.PlatformPresentationAbstractions_androidKt")
        every { getScreenWidthDp() } returns 480
        globalUi = mockk(relaxed = true)
        startKoinForPresenterUnitTests(globalUi)
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic("network.bisq.mobile.presentation.common.ui.platform.PlatformPresentationAbstractions_androidKt")
        try {
            stopKoin()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun makeModel(): OfferItemPresentationModel {
        val market = MarketVOFactory.USD
        val priceSpec = FixPriceSpecVO(with(PriceQuoteVOFactory) { fromPrice(100_000_00L, market) })
        val makerNetworkId =
            NetworkIdVO(
                AddressByTransportTypeMapVO(mapOf()),
                PubKeyVO(PublicKeyVO("pub"), keyId = "key", hash = "hash", id = "id"),
            )
        val fixedAmountSpec = QuoteSideFixedAmountSpecVO(amount = 500_000L)
        val offer =
            BisqEasyOfferVO(
                id = "offer-1",
                date = 0L,
                makerNetworkId = makerNetworkId,
                direction = DirectionEnum.BUY,
                market = market,
                amountSpec = fixedAmountSpec,
                priceSpec = priceSpec,
                protocolTypes = emptyList(),
                baseSidePaymentMethodSpecs = listOf(BitcoinPaymentMethodSpecVO("MAIN_CHAIN", null)),
                quoteSidePaymentMethodSpecs = listOf(FiatPaymentMethodSpecVO("SEPA", null)),
                offerOptions = emptyList(),
                supportedLanguageCodes = listOf("en"),
            )
        val user: UserProfileVO = createMockUserProfile("Alice")
        val reputation = ReputationScoreVO(0, 0.0, 0)
        return OfferItemPresentationModel(
            OfferItemPresentationDto(
                bisqEasyOffer = offer,
                isMyOffer = false,
                userProfile = user,
                formattedDate = "",
                formattedQuoteAmount = "",
                formattedBaseAmount = "",
                formattedPrice = "",
                formattedPriceSpec = "",
                quoteSidePaymentMethods = listOf("SEPA"),
                baseSidePaymentMethods = listOf("MAIN_CHAIN"),
                reputationScore = reputation,
            ),
        )
    }

    @Test
    fun `onTakeOffer shows then hides global loading`() =
        runTest(testDispatcher) {
            val marketUSD = MarketVOFactory.USD
            val marketItem =
                MarketPriceItem(
                    marketUSD,
                    with(PriceQuoteVOFactory) { fromPrice(100_000_00L, marketUSD) },
                    formattedPrice = "100000 USD",
                )
            val settingsRepo = TestSettings()
            val marketPrice = TestMarket(settingsRepo, mapOf(marketUSD to marketItem))

            val trades = mockk<TradesServiceFacade>(relaxed = true)
            every { trades.selectedTrade } returns MutableStateFlow(null)
            every { trades.openTradeItems } returns MutableStateFlow(emptyList())
            coEvery { trades.takeOffer(any(), any(), any(), any(), any(), any(), any()) } returns
                Result.success("trade-1")

            val coordinator = TakeOfferCoordinator(marketPrice, trades)
            coordinator.selectOfferToTake(makeModel())

            val main = mockk<MainPresenter>(relaxed = true)
            every { main.isDemo() } returns false

            val presenter = TakeOfferReviewPresenter(main, marketPrice, coordinator)
            presenter.onTakeOffer()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isTakeOfferLoading.value)
        }
}
