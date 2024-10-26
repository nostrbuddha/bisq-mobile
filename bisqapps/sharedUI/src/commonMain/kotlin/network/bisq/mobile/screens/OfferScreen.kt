package network.bisq.mobile.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import network.bisq.mobile.components.TopBar
import network.bisq.mobile.components.BuyOrSellCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferScreen(
    innerPadding: PaddingValues
) {
    val originDirection = LocalLayoutDirection.current
    Column(
        modifier = Modifier.fillMaxSize().padding(
            start = innerPadding.calculateStartPadding(originDirection),
            end = innerPadding.calculateEndPadding(originDirection),
            bottom = innerPadding.calculateBottomPadding(),
        ),
    ) {
        TopBar("Details Screen")
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Buy / Sell Toggle")
            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(100) {
                    BuyOrSellCard()
                }
            }
        }
    }
}