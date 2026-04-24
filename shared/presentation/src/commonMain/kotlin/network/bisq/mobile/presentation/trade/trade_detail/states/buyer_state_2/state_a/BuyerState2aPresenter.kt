package network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_2.state_a

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import network.bisq.mobile.data.replicated.presentation.open_trades.TradeItemPresentationModel
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.presentation.common.ui.base.BasePresenter
import network.bisq.mobile.presentation.main.MainPresenter

class BuyerState2aPresenter(
    mainPresenter: MainPresenter,
    private val tradesServiceFacade: TradesServiceFacade,
) : BasePresenter(mainPresenter) {
    val selectedTrade: StateFlow<TradeItemPresentationModel?> get() = tradesServiceFacade.selectedTrade

    private val _isConfirmFiatSentLoading = MutableStateFlow(false)
    val isConfirmFiatSentLoading = _isConfirmFiatSentLoading.asStateFlow()

    fun onConfirmFiatSent() {
        _isConfirmFiatSentLoading.value = true
        showLoading()
        presenterScope.launch {
            try {
                tradesServiceFacade
                    .buyerConfirmFiatSent()
                    .onFailure { handleError(it) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                hideLoading()
                _isConfirmFiatSentLoading.value = false
            }
        }
    }
}
