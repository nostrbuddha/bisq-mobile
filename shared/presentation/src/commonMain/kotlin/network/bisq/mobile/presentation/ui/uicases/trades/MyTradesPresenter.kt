package network.bisq.mobile.presentation.ui.uicases.trades

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.bisq.mobile.domain.data.BackgroundDispatcher
import network.bisq.mobile.domain.data.model.MockOffer
import network.bisq.mobile.domain.data.repository.MyTradesRepository
import network.bisq.mobile.presentation.BasePresenter
import network.bisq.mobile.presentation.MainPresenter
import network.bisq.mobile.presentation.ui.navigation.Routes

class MyTradesPresenter(
    mainPresenter: MainPresenter,
    private val myTradesRepository: MyTradesRepository
) : BasePresenter(mainPresenter), IMyTrades {

    private val _myTrades = MutableStateFlow<List<MockOffer>>(emptyList())
    override val myTrades: StateFlow<List<MockOffer>> = _myTrades

    override fun navigateToCurrencyList() {
        val tabController = getRootTabNavController()
        tabController.navigate(Routes.TabCurrencies.name) {
            tabController.graph.startDestinationRoute?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    private fun refresh() {
        CoroutineScope(BackgroundDispatcher).launch {
            try {
                delay(1000) // TODO: To simulate loading. Yet to be handled
                val trades = myTradesRepository.fetch()
                _myTrades.value = trades?.trades ?: emptyList()
            } catch (e: Exception) {
                // Handle errors
                println("Error: ${e.message}")
            }
        }
    }

    override fun onViewAttached() {
        super.onViewAttached()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }
}