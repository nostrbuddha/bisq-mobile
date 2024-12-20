package network.bisq.mobile.presentation.ui.navigation

import org.jetbrains.compose.resources.StringResource

object Graph {
    const val MAIN_SCREEN_GRAPH_KEY = "mainScreenGraph"
}

enum class Routes(val title: String) {
    Splash(title = "splash"),
    Onboarding(title = "onboarding"),
    CreateProfile(title = "create_profile"),
    TrustedNodeSetup(title = "trusted_node_setup"),
    TabContainer(title = "tab_container"),
    TabHome(title = "tab_home"),
    TabCurrencies(title = "tab_currencies"),
    TabMyTrades(title = "tab_my_trades"),
    TabSettings(title = "tab_settings"),
    OfferList(title = "offer_list"),
    TakeOfferTradeAmount(title = "take_offer_trade_amount"),
    TakeOfferPaymentMethod(title = "take_offer_payment_method"),
    TakeOfferReviewTrade(title = "take_offer_review_trade"),

    TradeFlow(title = "trade_flow"),
}