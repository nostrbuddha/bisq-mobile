package network.bisq.mobile.presentation.trade.trade_detail.states.buyer_state_3.state_b

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import network.bisq.mobile.data.replicated.presentation.open_trades.TradeItemPresentationModel
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.presentation.common.ui.base.BasePresenter
import network.bisq.mobile.presentation.main.MainPresenter

class BuyerStateLightning3bPresenter(
    mainPresenter: MainPresenter,
    private val tradesServiceFacade: TradesServiceFacade,
) : BasePresenter(mainPresenter) {
    val selectedTrade: StateFlow<TradeItemPresentationModel?> get() = tradesServiceFacade.selectedTrade

    private val _isCompleteTradeLoading = MutableStateFlow(false)
    val isCompleteTradeLoading = _isCompleteTradeLoading.asStateFlow()

    fun onCompleteTrade() {
        _isCompleteTradeLoading.value = true
        showLoading()
        presenterScope.launch {
            try {
                tradesServiceFacade
                    .btcConfirmed()
                    .onFailure { handleError(it) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                hideLoading()
                _isCompleteTradeLoading.value = false
            }
        }
    }
}
