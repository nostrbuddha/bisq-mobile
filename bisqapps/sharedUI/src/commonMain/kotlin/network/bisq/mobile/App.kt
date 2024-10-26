package network.bisq.mobile

import androidx.compose.material3.Scaffold
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import bisqapps.sharedui.generated.resources.Res
import bisqapps.sharedui.generated.resources.compose_multiplatform

import network.bisq.mobile.screens.ExchangeScreen
import network.bisq.mobile.screens.OfferScreen
import network.bisq.mobile.screens.CombinedExchangeScreen

@Composable
@Preview
fun App() {
    //Scaffold() { innerPadding -> ExchangeScreen(innerPadding) }
    Scaffold() { innerPadding -> OfferScreen(innerPadding) }
    //Scaffold() { innerPadding -> CombinedExchangeScreen()(innerPadding) }

    /*
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { GreetingProvider.factory.createGreeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
    */
}