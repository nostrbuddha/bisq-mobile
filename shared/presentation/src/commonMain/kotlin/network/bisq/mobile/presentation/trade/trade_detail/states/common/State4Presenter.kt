package network.bisq.mobile.presentation.trade.trade_detail.states.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.domain.repository.TradeReadStateRepository
import network.bisq.mobile.domain.trade.export.TradeCompletedCsv
import network.bisq.mobile.domain.trade.export.TradeExportCsvHeaders
import network.bisq.mobile.presentation.common.share.ShareFileService
import network.bisq.mobile.presentation.common.ui.base.BasePresenter
import network.bisq.mobile.presentation.common.ui.error.GenericErrorHandler
import network.bisq.mobile.presentation.main.MainPresenter

abstract class State4Presenter(
    mainPresenter: MainPresenter,
    private val tradesServiceFacade: TradesServiceFacade,
    private val tradeReadStateRepository: TradeReadStateRepository,
    private val shareFileService: ShareFileService,
) : BasePresenter(mainPresenter) {
    private val _uiState = MutableStateFlow(State4UiState())
    val uiState: StateFlow<State4UiState> = _uiState.asStateFlow()

    override fun onViewAttached() {
        super.onViewAttached()
        presenterScope.launch {
            tradesServiceFacade.selectedTrade.collect { trade ->
                _uiState.update {
                    it.copy(
                        trade = trade,
                        myDirectionLabel = resolveMyDirectionLabel(),
                        myOutcomeLabel = resolveMyOutcomeLabel(),
                    )
                }
            }
        }
    }

    override fun onViewUnattaching() {
        _uiState.update {
            it.copy(
                showCloseTradeDialog = false,
                isConfirmCloseTradeLoading = false,
                isExportTradeLoading = false,
            )
        }
        super.onViewUnattaching()
    }

    fun onAction(action: State4UiAction) {
        when (action) {
            State4UiAction.OnExportTradeClick -> onExportTrade()
            State4UiAction.OnCloseTradeClick -> onCloseTrade()
            State4UiAction.OnDismissCloseTrade -> onDismissCloseTrade()
            State4UiAction.OnConfirmCloseTrade -> onConfirmCloseTrade()
        }
    }

    private fun onCloseTrade() {
        _uiState.update { it.copy(showCloseTradeDialog = true) }
    }

    private fun onDismissCloseTrade() {
        _uiState.update { it.copy(showCloseTradeDialog = false, isConfirmCloseTradeLoading = false) }
    }

    private fun onConfirmCloseTrade() {
        presenterScope.launch {
            val tradeId =
                _uiState.value.trade?.tradeId ?: run {
                    _uiState.update { it.copy(showCloseTradeDialog = false) }
                    GenericErrorHandler.handleGenericError("No trade selected for closure")
                    return@launch
                }
            _uiState.update { it.copy(isConfirmCloseTradeLoading = true) }
            showLoading()
            try {
                val result = tradesServiceFacade.closeTrade()
                when {
                    result.isFailure -> {
                        _uiState.update { it.copy(showCloseTradeDialog = false) }
                        result
                            .exceptionOrNull()
                            ?.let { exception -> GenericErrorHandler.handleGenericError(exception.message) }
                            ?: GenericErrorHandler.handleGenericError("No Exception is set in result failure")
                    }

                    result.isSuccess -> {
                        withContext(Dispatchers.IO) {
                            tradeReadStateRepository.clearId(tradeId)
                        }
                        _uiState.update { it.copy(showCloseTradeDialog = false) }
                        navigateBack()
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(showCloseTradeDialog = false) }
                GenericErrorHandler.handleGenericError(e.message)
            } finally {
                hideLoading()
                _uiState.update { it.copy(isConfirmCloseTradeLoading = false) }
            }
        }
    }

    private fun onExportTrade() {
        log.w { "onExportTrade - BEGIN" }
        val trade =
            _uiState.value.trade ?: run {
                GenericErrorHandler.handleGenericError("No trade selected for export")
                return
            }
        _uiState.update { it.copy(isExportTradeLoading = true) }
        showLoading()
        presenterScope.launch {
            try {
                val headers = TradeExportCsvHeaders.resolveForTrade(trade)
                val csv =
                    withContext(Dispatchers.Default) {
                        TradeCompletedCsv.buildCsv(trade, headers)
                    }
                val fileName = "BisqEasy-trade-${trade.shortTradeId}.csv"
                val result = shareFileService.shareUtf8TextFile(csv, fileName)
                if (result.isFailure) {
                    result.exceptionOrNull()?.let { e ->
                        GenericErrorHandler.handleGenericError(e.message)
                    } ?: GenericErrorHandler.handleGenericError("Trade export failed")
                }
            } catch (e: Exception) {
                GenericErrorHandler.handleGenericError(e.message)
            } finally {
                hideLoading()
                _uiState.update { it.copy(isExportTradeLoading = false) }
            }
        }
    }

    protected abstract fun resolveMyDirectionLabel(): String

    protected abstract fun resolveMyOutcomeLabel(): String
}
