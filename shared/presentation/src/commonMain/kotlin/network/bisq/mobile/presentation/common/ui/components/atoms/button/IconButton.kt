package network.bisq.mobile.presentation.common.ui.components.atoms.button

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants

@Composable
fun BisqIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    isLoading: Boolean = false,
    size: Dp = BisqUIConstants.ScreenPadding2X,
    content: @Composable () -> Unit,
) {
    val enabled = !disabled && !isLoading
    IconButton(
        modifier =
            modifier
                .size(size)
                .alpha(if (!enabled) 0.5f else 1.0f),
        onClick = onClick,
        colors =
            IconButtonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = BisqTheme.colors.white,
                disabledContentColor = BisqTheme.colors.mid_grey20,
            ),
        enabled = enabled,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size * 0.55f),
                color = BisqTheme.colors.white,
                strokeWidth = 2.dp,
            )
        } else {
            content()
        }
    }
}
