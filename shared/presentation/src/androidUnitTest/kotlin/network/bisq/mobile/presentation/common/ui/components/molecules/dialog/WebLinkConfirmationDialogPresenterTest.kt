package network.bisq.mobile.presentation.common.ui.components.molecules.dialog

import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.UriHandler
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import network.bisq.mobile.data.service.settings.SettingsServiceFacade
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.test_utils.startKoinForPresenterUnitTests
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.main.MainPresenter
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebLinkConfirmationDialogPresenterTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var globalUi: GlobalUiManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        I18nSupport.initialize("en")
        globalUi = mockk(relaxed = true)
        startKoinForPresenterUnitTests(globalUi)
    }

    @AfterTest
    fun tearDown() {
        try {
            stopKoin()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `on confirm with persist shows then hides global loading`() =
        runTest(testDispatcher) {
            val settings = mockk<SettingsServiceFacade>(relaxed = true)
            every { settings.showWebLinkConfirmation } returns MutableStateFlow(true)
            every { settings.permitOpeningBrowser } returns MutableStateFlow(true)
            coEvery { settings.setPermitOpeningBrowser(any()) } returns Result.success(Unit)
            coEvery { settings.setWebLinkDontShowAgain() } returns Result.success(Unit)

            val main = mockk<MainPresenter>(relaxed = true)
            val presenter = WebLinkConfirmationDialogPresenter(settings, main)
            val uriHandler = mockk<UriHandler>(relaxed = true)
            val clipboard = mockk<Clipboard>(relaxed = true)

            presenter.initialize(
                link = "https://example.com",
                uriHandler = uriHandler,
                clipboard = clipboard,
                onConfirm = {},
                onDismiss = {},
                onError = {},
                forceConfirm = true,
            )
            presenter.onAction(WebLinkConfirmationUiAction.OnConfirm)
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
        }
}
