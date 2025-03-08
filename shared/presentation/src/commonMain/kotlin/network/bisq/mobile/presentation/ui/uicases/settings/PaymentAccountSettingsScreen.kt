package network.bisq.mobile.presentation.ui.uicases.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.lyricist.LocalStrings
import kotlinx.coroutines.flow.StateFlow
import network.bisq.mobile.domain.data.replicated.account.UserDefinedFiatAccountVO
import network.bisq.mobile.presentation.ViewPresenter
import network.bisq.mobile.presentation.ui.components.atoms.*
import network.bisq.mobile.presentation.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.ui.components.molecules.BisqBottomSheet
import network.bisq.mobile.presentation.ui.components.molecules.ConfirmationDialog
import network.bisq.mobile.presentation.ui.components.organisms.settings.AppPaymentAccountCard
import network.bisq.mobile.presentation.ui.helpers.RememberPresenterLifecycle
import network.bisq.mobile.presentation.ui.theme.BisqUIConstants
import org.koin.compose.koinInject

interface IPaymentAccountSettingsPresenter : ViewPresenter {
    val accounts: StateFlow<List<UserDefinedFiatAccountVO>>
    val selectedAccount: StateFlow<UserDefinedFiatAccountVO?>

    fun selectAccount(account: UserDefinedFiatAccountVO)

    fun addAccount(newName: String, newDescription: String)
    fun saveAccount(newName: String, newDescription: String)
    fun deleteCurrentAccount()
}

// TODO: Toast messages
@Composable
fun PaymentAccountSettingsScreen() {

    val strings = LocalStrings.current.user
    val stringsCommon = LocalStrings.current.common

    val presenter: IPaymentAccountSettingsPresenter = koinInject()
    val accounts by presenter.accounts.collectAsState()
    val selectedAccount by presenter.selectedAccount.collectAsState()

    var accountName by remember { mutableStateOf(selectedAccount?.accountName ?: "") }
    var accountNameValid by remember { mutableStateOf(true) }
    var accountDescription by remember { mutableStateOf(selectedAccount?.accountPayload?.accountData ?: "") }
    var accountDescriptionValid by remember { mutableStateOf(true) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    RememberPresenterLifecycle(presenter)

    LaunchedEffect(selectedAccount) {
        accountName = selectedAccount?.accountName ?: ""
        accountDescription = selectedAccount?.accountPayload?.accountData ?: ""
    }

    if (showBottomSheet) {
        BisqBottomSheet(
            onDismissRequest = { showBottomSheet = !showBottomSheet }
        ) {
            AppPaymentAccountCard(
                onCancel = { showBottomSheet = false },
                onConfirm = { name, description ->
                    presenter.addAccount(name, description)
                    showBottomSheet = false
                },
            )
        }
    }

    if (accounts.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            BisqButton(
                text = strings.user_paymentAccounts_createAccount,
                onClick = { showBottomSheet = !showBottomSheet },
                modifier = Modifier.padding(all = 8.dp)
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPadding)
    ) {
        BisqButton(
            text = strings.user_paymentAccounts_createAccount,
            onClick = { showBottomSheet = !showBottomSheet },
            padding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            modifier = Modifier.align(Alignment.End)
        )

        BisqGap.V1()

        BisqEditableDropDown(
            value = accountName,
            onValueChanged = { name, isValid ->
                println("name: $name")
                var account = accounts.firstOrNull { it.accountName == name }
                if (account == null) {
                    account = accounts.firstOrNull { it.accountPayload.accountData == accountDescription }
                }
                if (account != null) {
                    presenter.selectAccount(account)
                }
                accountName = name
                accountNameValid = isValid
            },
            items = accounts.map { it.accountName },
            label = strings.user_userProfile_payment_account,
            validation = {
                if (it.length < 3) {
                    return@BisqEditableDropDown "Min length: 3 characters"
                }

                if (it.length > 1024) {
                    return@BisqEditableDropDown "Max length: 1024 characters"
                }

                return@BisqEditableDropDown null
            }
        )

        BisqGap.V1()

        BisqTextField(
            value = accountDescription,
            onValueChange = { value, isValid ->
                accountDescription = value
                accountDescriptionValid = isValid
            },
            label = strings.user_paymentAccounts_accountData,
            isTextArea = true,
            validation = {

                if (it.length < 3) {
                    return@BisqTextField "Min length: 3 characters"
                }

                if (it.length > 1024) {
                    return@BisqTextField "Max length: 1024 characters"
                }

                return@BisqTextField null
            }
        )

        BisqGap.V1()

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            BisqButton(
                text = stringsCommon.delete_account,
                type = BisqButtonType.Grey,
                onClick = { showConfirmationDialog = true },
                disabled = selectedAccount == null
            )
            BisqButton(
                text = stringsCommon.buttons_save,
                onClick = {
                    presenter.saveAccount(accountName, accountDescription)
                },
                disabled = !accountNameValid || !accountDescriptionValid
            )
        }
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            onConfirm = {
                presenter.deleteCurrentAccount()
                accountName = presenter.selectedAccount.value?.accountName ?: ""
                accountDescription = presenter.selectedAccount.value?.accountPayload?.accountData ?: ""
                showConfirmationDialog = false
            },
            onDismiss = {
                showConfirmationDialog = false
            }
        )
    }

}
