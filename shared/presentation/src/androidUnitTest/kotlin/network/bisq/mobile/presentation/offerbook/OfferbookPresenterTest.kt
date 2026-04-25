package network.bisq.mobile.presentation.offerbook

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
import network.bisq.mobile.data.model.offerbook.MarketListItem
import network.bisq.mobile.data.model.offerbook.OfferbookMarket
import network.bisq.mobile.data.replicated.common.currency.MarketVO
import network.bisq.mobile.data.replicated.common.monetary.PriceQuoteVOFactory
import network.bisq.mobile.data.replicated.common.network.AddressByTransportTypeMapVO
import network.bisq.mobile.data.replicated.network.identity.NetworkIdVO
import network.bisq.mobile.data.replicated.offer.DirectionEnum
import network.bisq.mobile.data.replicated.offer.amount.spec.QuoteSideRangeAmountSpecVO
import network.bisq.mobile.data.replicated.offer.bisq_easy.BisqEasyOfferVO
import network.bisq.mobile.data.replicated.offer.price.spec.FixPriceSpecVO
import network.bisq.mobile.data.replicated.presentation.offerbook.OfferItemPresentationDto
import network.bisq.mobile.data.replicated.presentation.offerbook.OfferItemPresentationModel
import network.bisq.mobile.data.replicated.security.keys.PubKeyVO
import network.bisq.mobile.data.replicated.security.keys.PublicKeyVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.data.service.alert.TradeRestrictingAlertServiceFacade
import network.bisq.mobile.data.service.market_price.MarketPriceServiceFacade
import network.bisq.mobile.data.service.offers.OffersServiceFacade
import network.bisq.mobile.data.service.reputation.ReputationServiceFacade
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.domain.utils.CoroutineJobsManager
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.test_utils.NoopNavigationManager
import network.bisq.mobile.presentation.common.test_utils.TestCoroutineJobsManager
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.common.ui.navigation.manager.NavigationManager
import network.bisq.mobile.presentation.common.ui.platform.getScreenWidthDp
import network.bisq.mobile.presentation.main.MainPresenter
import network.bisq.mobile.presentation.offer.create_offer.CreateOfferCoordinator
import network.bisq.mobile.presentation.offer.take_offer.TakeOfferCoordinator
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class OfferbookPresenterTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var globalUi: GlobalUiManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        I18nSupport.initialize("en")
        mockkStatic("network.bisq.mobile.presentation.common.ui.platform.PlatformPresentationAbstractions_androidKt")
        every { getScreenWidthDp() } returns 480
        globalUi = mockk(relaxed = true)
        startKoin {
            modules(
                module {
                    factory<CoroutineJobsManager> { TestCoroutineJobsManager(testDispatcher) }
                    single<NavigationManager> { NoopNavigationManager() }
                    single<GlobalUiManager> { globalUi }
                },
            )
        }
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

    private fun makeOffer(
        id: String,
        isMy: Boolean,
    ): OfferItemPresentationModel {
        val market = MarketVO("BTC", "USD", "Bitcoin", "US Dollar")
        val amountSpec = QuoteSideRangeAmountSpecVO(minAmount = 10_0000L, maxAmount = 100_0000L)
        val priceSpec = FixPriceSpecVO(with(PriceQuoteVOFactory) { fromPrice(100_00L, market) })
        val makerNetworkId =
            NetworkIdVO(
                AddressByTransportTypeMapVO(mapOf()),
                PubKeyVO(PublicKeyVO("pub"), keyId = id, hash = id, id = id),
            )
        val offer =
            BisqEasyOfferVO(
                id = id,
                date = 0L,
                makerNetworkId = makerNetworkId,
                direction = DirectionEnum.SELL,
                market = market,
                amountSpec = amountSpec,
                priceSpec = priceSpec,
                protocolTypes = emptyList(),
                baseSidePaymentMethodSpecs = emptyList(),
                quoteSidePaymentMethodSpecs = emptyList(),
                offerOptions = emptyList(),
                supportedLanguageCodes = listOf("en"),
            )
        val user: UserProfileVO = createMockUserProfile("maker-$id")
        val reputation = ReputationScoreVO(0, 0.0, 0)
        val dto =
            OfferItemPresentationDto(
                bisqEasyOffer = offer,
                isMyOffer = isMy,
                userProfile = user,
                formattedDate = "",
                formattedQuoteAmount = "",
                formattedBaseAmount = "",
                formattedPrice = "",
                formattedPriceSpec = "",
                quoteSidePaymentMethods = listOf("SEPA"),
                baseSidePaymentMethods = listOf("MAIN_CHAIN"),
                reputationScore = reputation,
            )
        return OfferItemPresentationModel(dto)
    }

    private fun buildPresenter(allOffers: List<OfferItemPresentationModel>): OfferbookPresenter {
        val mainPresenter = mockk<MainPresenter>(relaxed = true)
        every { mainPresenter.isDemo() } returns false
        val offersFlow = MutableStateFlow(allOffers)
        val marketFlow =
            MutableStateFlow(
                OfferbookMarket(
                    MarketVO(
                        baseCurrencyCode = "BTC",
                        quoteCurrencyCode = "USD",
                        baseCurrencyName = "Bitcoin",
                        quoteCurrencyName = "US Dollar",
                    ),
                ),
            )
        val offersService = mockk<OffersServiceFacade>()
        every { offersService.offerbookListItems } returns offersFlow
        every { offersService.selectedOfferbookMarket } returns marketFlow
        coEvery { offersService.deleteOffer(any()) } returns Result.success(true)
        val userProfiles = mockk<UserProfileServiceFacade>(relaxed = true)
        val me = createMockUserProfile("me")
        every { userProfiles.selectedUserProfile } returns MutableStateFlow(me)
        coEvery { userProfiles.isUserIgnored(any()) } returns false
        coEvery { userProfiles.getUserProfileIcon(any(), any()) } returns mockk(relaxed = true)
        coEvery { userProfiles.getUserProfileIcon(any()) } returns mockk(relaxed = true)
        val marketPriceServiceFacade =
            object : MarketPriceServiceFacade(mockk(relaxed = true)) {
                override fun findMarketPriceItem(marketVO: MarketVO) = null

                override fun findUSDMarketPriceItem() = null

                override fun refreshSelectedFormattedMarketPrice() {}

                override fun selectMarket(marketListItem: MarketListItem): Result<Unit> = Result.success(Unit)
            }
        val takeOfferCoordinator = mockk<TakeOfferCoordinator>(relaxed = true)
        val createOfferCoordinator = mockk<CreateOfferCoordinator>(relaxed = true)
        val tradeRestricting = mockk<TradeRestrictingAlertServiceFacade>(relaxed = true)
        every { tradeRestricting.alert } returns MutableStateFlow(null)
        return OfferbookPresenter(
            mainPresenter,
            offersService,
            takeOfferCoordinator,
            createOfferCoordinator,
            marketPriceServiceFacade,
            userProfiles,
            mockk<ReputationServiceFacade>(relaxed = true),
            tradeRestricting,
        )
    }

    @Test
    fun `createOffer shows then hides global loading`() =
        runTest(testDispatcher) {
            val myOffer = makeOffer("o1", isMy = true)
            val presenter = buildPresenter(listOf(myOffer))
            presenter.onViewAttached()
            advanceUntilIdle()

            presenter.createOffer()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
            assertFalse(presenter.isCreateOfferInFlight.value)
        }

    @Test
    fun `onConfirmedDeleteOffer shows then hides global loading`() =
        runTest(testDispatcher) {
            val myOffer = makeOffer("o1", isMy = true)
            val presenter = buildPresenter(listOf(myOffer))
            presenter.onViewAttached()
            advanceUntilIdle()
            presenter.onOfferSelected(myOffer)
            advanceUntilIdle()

            presenter.onConfirmedDeleteOffer()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
        }
}
