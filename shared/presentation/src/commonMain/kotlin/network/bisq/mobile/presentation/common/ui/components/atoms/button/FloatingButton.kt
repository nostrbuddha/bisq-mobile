package network.bisq.mobile.presentation.common.ui.components.atoms.button

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import network.bisq.mobile.presentation.common.ui.components.atoms.icons.AddIcon
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants

@Composable
fun FloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(BisqUIConstants.ScreenPadding4X),
    enabled: Boolean = true,
    isLoading: Boolean = false,
    content: @Composable () -> Unit,
) {
    val interactive = enabled && !isLoading
    FloatingActionButton(
        modifier = modifier,
        onClick = { if (interactive) onClick() },
        containerColor = if (interactive) BisqTheme.colors.primary else Color.Gray.copy(alpha = 0.5f),
        contentColor = if (interactive) BisqTheme.colors.white else Color.LightGray,
        shape = CircleShape,
    ) {
        content()
    }
}

@Composable
fun BisqFABAddButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    FloatingButton(
        onClick = onClick,
        enabled = enabled,
        isLoading = isLoading,
    ) {
        AddIcon()
    }
}
