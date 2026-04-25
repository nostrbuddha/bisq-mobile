package network.bisq.mobile.presentation.startup.user_agreement

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserAgreementPresenterTest {
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
    fun `onAcceptTerms shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val settings = mockk<SettingsServiceFacade>(relaxed = true)
            coEvery { settings.confirmTacAccepted(true) } returns Result.success(Unit)

            val presenter = UserAgreementPresenter(main, settings)
            assertTrue { !presenter.isAccepted.value }
            presenter.onAcceptTerms()
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
        }
}
