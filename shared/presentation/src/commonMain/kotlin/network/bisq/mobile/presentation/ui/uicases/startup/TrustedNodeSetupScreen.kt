package network.bisq.mobile.presentation.ui.uicases.startup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import network.bisq.mobile.presentation.ViewPresenter
import network.bisq.mobile.presentation.ui.components.atoms.BisqButton
import network.bisq.mobile.presentation.ui.components.atoms.BisqButtonType
import network.bisq.mobile.presentation.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.ui.components.atoms.BisqTextField
import network.bisq.mobile.presentation.ui.components.atoms.icons.BisqLogo
import network.bisq.mobile.presentation.ui.components.atoms.icons.CopyIcon
import network.bisq.mobile.presentation.ui.components.atoms.icons.QuestionIcon
import network.bisq.mobile.presentation.ui.components.layout.BisqScrollScaffold
import network.bisq.mobile.presentation.ui.components.molecules.TopBar
import network.bisq.mobile.presentation.ui.components.molecules.settings.BreadcrumbNavigation
import network.bisq.mobile.presentation.ui.components.molecules.settings.MenuItem
import network.bisq.mobile.presentation.ui.helpers.RememberPresenterLifecycle
import network.bisq.mobile.presentation.ui.theme.BisqTheme
import network.bisq.mobile.presentation.ui.uicases.settings.ISettingsPresenter
import org.koin.compose.koinInject

interface ITrustedNodeSetupPresenter : ViewPresenter {
    val isBisqApiUrlValid: StateFlow<Boolean>
    val bisqApiUrl: StateFlow<String>
    val isConnected: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>

    fun updateBisqApiUrl(newUrl: String, isValid: Boolean)

    /**
     * @return the error message if url is invalid, null otherwise
     */
    fun validateWsUrl(url: String): String?

    fun testConnection(isWorkflow: Boolean = true)

    fun navigateToNextScreen()

    fun goBackToSetupScreen()
}

@Composable
fun TrustedNodeSetupScreen(isWorkflow: Boolean = true) {
    val presenter: ITrustedNodeSetupPresenter = koinInject()
    val settingsPresenter: ISettingsPresenter = koinInject()

    val bisqApiUrl = presenter.bisqApiUrl.collectAsState().value
    val isConnected = presenter.isConnected.collectAsState().value
    val isLoading = presenter.isLoading.collectAsState().value
    val clipboardManager = LocalClipboardManager.current

    val menuTree: MenuItem = settingsPresenter.menuTree()
    val menuPath = remember { mutableStateListOf(menuTree) }

    RememberPresenterLifecycle(presenter, {
        if (!isWorkflow) {
            menuPath.add((menuTree as MenuItem.Parent).children[3])
        }
    })

    BisqScrollScaffold(
        topBar = { TopBar("Trusted node") }, // TODO:i18n
        snackbarHostState = presenter.getSnackState()
    ) {
        if (isWorkflow) {
            BisqLogo()
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            BreadcrumbNavigation(path = menuPath) { index ->
                if (index == 0) settingsPresenter.settingsNavigateBack()
            }
        }
        BisqText.largeRegular(
            text = "To use Bisq through your trusted node, please enter the URL to connect to. E.g. ws://10.0.2.2:8090",
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize().padding(horizontal = 0.dp)
        ) {
            BisqTextField(
                label = "Trusted Bisq Node URL",
                onValueChange = { url, isValid -> presenter.updateBisqApiUrl(url, isValid) },
                value = bisqApiUrl,
                placeholder = "ws://10.0.2.2:8090",
                keyboardType = KeyboardType.Uri,
                disabled = isLoading,
                labelRightSuffix = {
                    BisqButton(
                        iconOnly = { QuestionIcon() },
                        backgroundColor = BisqTheme.colors.backgroundColor,
                        onClick = { /* presenter.navigateToNextScreen() */ },
                        disabled = isLoading,
                    )
                },
                validation = {
                    return@BisqTextField presenter.validateWsUrl(it)
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BisqButton(
                    text = "Paste",
                    type = BisqButtonType.Grey,
                    onClick = {
                        val annotatedString = clipboardManager.getText()
                        if (annotatedString != null) {
                            presenter.updateBisqApiUrl(annotatedString.text, false) // TODO: validation gets triggered?
                        }
                    },
                    disabled = isLoading,
                    color = BisqTheme.colors.light1,
                    leftIcon = { CopyIcon() }
                )
//              TODO uncomment when feature gets implemented
//                BisqButton(
//                    text = "Scan",
//                    onClick = {},
//                    leftIcon= { ScanIcon() }
//                )
            }
            Spacer(modifier = Modifier.height(36.dp))
            BisqText.baseRegularGrey("STATUS")
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BisqText.largeRegular(
                    if (isConnected) "Connected" else "Not Connected",
                )
                Spacer(modifier = Modifier.width(12.dp))
                BisqText.baseRegular(
                    text = "",
                    modifier = Modifier.clip(
                        RoundedCornerShape(5.dp)
                    ).background(color = if (isConnected) BisqTheme.colors.primary else BisqTheme.colors.danger)
                        .size(10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(56.dp))

        if (!isConnected) {
            BisqButton(
                text = "Test Connection",
                onClick = {
                    presenter.testConnection()
                },
                padding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                disabled = !presenter.isBisqApiUrlValid.collectAsState().value,
                isLoading = isLoading,
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)
            ) {
                AnimatedVisibility(
                    visible = isConnected,
                    enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(700)),
                ) {
                    BisqButton(
                        text = "Test Connection",
                        color = if (bisqApiUrl.isEmpty()) BisqTheme.colors.grey1 else BisqTheme.colors.light1,
                        onClick = { presenter.testConnection() },
                        padding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                    )
                }
                AnimatedVisibility(
                    visible = isConnected,
                    enter = fadeIn(animationSpec = tween(300)),
                ) {
                    BisqButton(
                        text = if (isWorkflow) "Next" else "Save",
                        color = BisqTheme.colors.light1,
                        onClick = { if (isWorkflow) presenter.navigateToNextScreen() else presenter.testConnection(false) },
                        padding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}
