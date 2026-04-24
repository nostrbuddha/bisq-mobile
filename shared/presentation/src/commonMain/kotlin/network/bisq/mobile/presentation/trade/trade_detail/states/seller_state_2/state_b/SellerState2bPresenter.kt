package network.bisq.mobile.presentation.trade.trade_detail.states.seller_state_2.state_b

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import network.bisq.mobile.data.replicated.presentation.open_trades.TradeItemPresentationModel
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.presentation.common.ui.base.BasePresenter
import network.bisq.mobile.presentation.main.MainPresenter

class SellerState2bPresenter(
    mainPresenter: MainPresenter,
    private val tradesServiceFacade: TradesServiceFacade,
) : BasePresenter(mainPresenter) {
    val selectedTrade: StateFlow<TradeItemPresentationModel?> get() = tradesServiceFacade.selectedTrade

    private val _isConfirmFiatReceiptLoading = MutableStateFlow(false)
    val isConfirmFiatReceiptLoading = _isConfirmFiatReceiptLoading.asStateFlow()

    fun onConfirmFiatReceipt() {
        _isConfirmFiatReceiptLoading.value = true
        showLoading()
        presenterScope.launch {
            try {
                tradesServiceFacade
                    .sellerConfirmFiatReceipt()
                    .onFailure { handleError(it) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                hideLoading()
                _isConfirmFiatReceiptLoading.value = false
            }
        }
    }
}
