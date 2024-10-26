package network.bisq.mobile.components

import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import bisqapps.sharedui.generated.resources.Res
import bisqapps.sharedui.generated.resources.filter
import network.bisq.mobile.theme.primaryColor
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = primaryColor,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(title, color = Color.White)
        },
        actions = {
//            Image(painterResource(Res.drawable.filter), null)
//            IconButton(onClick = { /* do something */ }) {
//                //Image(painterResource(Res.drawable.filter), null)
//                Icon(
//                    tint = Color.White,
//                    painter = painterResource(Res.drawable.filter),
//                    contentDescription = "Localized description",
//                )
//            }
        },
    )
}