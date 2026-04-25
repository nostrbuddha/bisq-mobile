package network.bisq.mobile.presentation.startup.onboarding

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
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.domain.repository.SettingsRepository
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.test_utils.startKoinForPresenterUnitTests
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.main.MainPresenter
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingPresenterTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var globalUi: GlobalUiManager

    private class TestOnboardingPresenter(
        mainPresenter: MainPresenter,
        settingsRepository: SettingsRepository,
        userProfileService: UserProfileServiceFacade,
    ) : OnboardingPresenter(mainPresenter, settingsRepository, userProfileService) {
        override val headline: String = "headline"
        override val indexesToShow: List<Int> = listOf(0)
    }

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
    fun `on next on last page shows then hides global loading`() =
        runTest(testDispatcher) {
            val main = mockk<MainPresenter>(relaxed = true)
            val settings = mockk<SettingsRepository>(relaxed = true)
            coEvery { settings.setFirstLaunch(false) } coAnswers { }
            val users = mockk<UserProfileServiceFacade>(relaxed = true)
            coEvery { users.hasUserProfile() } returns true

            val presenter = TestOnboardingPresenter(main, settings, users)
            presenter.onViewAttached()
            advanceUntilIdle()

            presenter.onAction(OnboardingUiAction.OnNextButtonClick)
            advanceUntilIdle()

            verify(exactly = 1) { globalUi.scheduleShowLoading() }
            verify(exactly = 1) { globalUi.hideLoading() }
        }
}
