package network.bisq.mobile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import bisqapps.sharedui.generated.resources.Res
import bisqapps.sharedui.generated.resources.bitcoin
import bisqapps.sharedui.generated.resources.ic_star_empty
import bisqapps.sharedui.generated.resources.profile
import network.bisq.mobile.theme.progressColor
import org.jetbrains.compose.resources.painterResource

@Composable
fun BuyOrSellCard() {
    Column(modifier = Modifier.border(width = 1.dp, color = progressColor, shape = RoundedCornerShape(6.dp))) {
        Column (modifier = Modifier.padding(10.dp)){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Image(
//                        contentScale = ContentScale.FillBounds,
//                        modifier = Modifier.size(32.dp).clip(shape = RoundedCornerShape(16.dp)),
//                        painter = painterResource(resource = Res.drawable.profile),
//                        contentDescription = null,
//                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Satoshi Ninja")
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                            Icon(
//                                tint = progressColor,
//                                painter = painterResource(resource = Res.drawable.ic_star_empty),
//                                contentDescription = "Localized description",
//                            )
//                            Icon(
//                                tint = progressColor,
//                                painter = painterResource(resource = Res.drawable.ic_star_empty),
//                                contentDescription = "Localized description",
//                            )
//                            Icon(
//                                tint = progressColor,
//                                painter = painterResource(resource = Res.drawable.ic_star_empty),
//                                contentDescription = "Localized description",
//                            )
//                            Icon(
//                                tint = progressColor,
//                                painter = painterResource(resource = Res.drawable.ic_star_empty),
//                                contentDescription = "Localized description",
//                            )
//                            Icon(
//                                tint = progressColor,
//                                painter = painterResource(resource = Res.drawable.ic_star_empty),
//                                contentDescription = "Localized description",
//                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("1.00%")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("$50 - $500")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
//                Image(
//                    modifier = Modifier.size(24.dp),
//                    painter = painterResource(resource = Res.drawable.bitcoin),
//                    contentDescription = "Localized description",
//                )
//                Image(
//                    modifier = Modifier.size(24.dp),
//                    painter = painterResource(resource = Res.drawable.bitcoin),
//                    contentDescription = "Localized description",
//                )
//                Image(
//                    modifier = Modifier.size(24.dp),
//                    painter = painterResource(resource = Res.drawable.bitcoin),
//                    contentDescription = "Localized description",
//                )
//                Icon(
//                    tint = Color.White,
//                    imageVector = Icons.Filled.ArrowForward,
//                    contentDescription = "Localized description",
//                )
//
//                Image(
//                    modifier = Modifier.size(24.dp),
//                    painter = painterResource(resource = Res.drawable.bitcoin),
//                    contentDescription = "Localized description",
//                )
//                Image(
//                    modifier = Modifier.size(24.dp),
//                    painter = painterResource(resource = Res.drawable.bitcoin),
//                    contentDescription = "Localized description",
//                )
            }
        }
    }
}