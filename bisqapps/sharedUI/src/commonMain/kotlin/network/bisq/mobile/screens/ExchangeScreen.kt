package network.bisq.mobile.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import bisqapps.sharedui.generated.resources.Res
import bisqapps.sharedui.generated.resources.filter
import bisqapps.sharedui.generated.resources.market_cad
import bisqapps.sharedui.generated.resources.market_eur
import bisqapps.sharedui.generated.resources.market_gbp
import bisqapps.sharedui.generated.resources.market_sgd
import bisqapps.sharedui.generated.resources.market_usd
import network.bisq.mobile.components.TopBar
import network.bisq.mobile.theme.primaryTextColor
import network.bisq.mobile.theme.secondaryTextColor

val flagResources = listOf(
    Res.drawable.market_cad,
    Res.drawable.market_eur,
    Res.drawable.market_gbp,
    Res.drawable.market_sgd,
    Res.drawable.market_usd,
)

val currencies = listOf(
    Pair("Canadian Dolalrs", "CAD"),
    Pair("Euro", "EUR"),
    Pair("British Pound", "GBP"),
    Pair("Singaporian Dolalrs", "SGD"),
    Pair("United States Dolalrs", "USD"),
)

@Composable
fun ExchangeScreen(
    innerPadding: PaddingValues
) {
    val originDirection = LocalLayoutDirection.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(
            start = innerPadding.calculateStartPadding(originDirection),
            end = innerPadding.calculateEndPadding(originDirection),
            bottom = innerPadding.calculateBottomPadding(),
        )
    ) {
        TopBar("Exchange")
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)
                .fillMaxHeight()
        ) {
            var text by remember { mutableStateOf("") }
            // Image(painterResource(Res.drawable.filter), null)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().focusable(false),
                maxLines = 1,
                textStyle = TextStyle(color = Color.White),
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Search...", color = primaryTextColor) },
            )
            // Image(painterResource(Res.drawable.filter), null)
            LazyColumn {
                items(5) { index->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
//                            Image(
//                                contentScale = ContentScale.FillBounds,
//                                modifier = Modifier.size(50.dp).clip(shape = RoundedCornerShape(25.dp)),
//                                painter = painterResource(resource = flagResources[index]),
//                                contentDescription = null,
//                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = currencies[index].first, color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "${currencies[index].second}(43 offers)", color = secondaryTextColor)
                            }
                        }

                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                tint = Color.White,
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Localized description",
                            )
                        }
                    }
                }
            }
        }
    }
}