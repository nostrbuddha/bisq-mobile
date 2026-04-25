package network.bisq.mobile.presentation.settings.user_profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.data.service.reputation.ReputationServiceFacade
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.data.utils.PlatformImage
import network.bisq.mobile.domain.utils.CoroutineJobsManager
import network.bisq.mobile.domain.utils.DefaultCoroutineJobsManager
import network.bisq.mobile.domain.utils.TimeUtils
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.common.ui.navigation.manager.NavigationManager
import network.bisq.mobile.presentation.main.MainPresenter
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Exercises [UserProfilePresenter] save/loading without hanging on
 * [TimeUtils.tickerFlow] (mocked to an empty base flow) and asserts global loading + [UserProfileUiState.isBusyWithAction].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserProfilePresenterSaveLoadingTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var globalUiManager: GlobalUiManager
    private lateinit var userProfileServiceFacade: UserProfileServiceFacade
    private lateinit var reputationServiceFacade: ReputationServiceFacade
    private lateinit var mainPresenter: MainPresenter

    private val profile1 = createMockUserProfile("Alice")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        I18nSupport.initialize("en")
        globalUiManager = mockk(relaxed = true)
        userProfileServiceFacade = mockk(relaxed = true)
        reputationServiceFacade = mockk(relaxed = true)
        mainPresenter = mockk(relaxed = true)
        every { userProfileServiceFacade.userProfiles } returns MutableStateFlow(listOf(profile1))
        every { userProfileServiceFacade.selectedUserProfile } returns MutableStateFlow(profile1)
        every { userProfileServiceFacade.ignoredProfileIds } returns MutableStateFlow(emptySet())
        every { userProfileServiceFacade.numUserProfiles } returns MutableStateFlow(1)
        coEvery { userProfileServiceFacade.getUserProfileIcon(any(), any()) } returns
            mockk<PlatformImage>(relaxed = true)
        coEvery { userProfileServiceFacade.getUserPublishDate() } returns 0L
        coEvery { reputationServiceFacade.getReputation(any()) } returns
            Result.success(
                ReputationScoreVO(totalScore = 100L, fiveSystemScore = 50.0, ranking = 10),
            )
        coEvery { reputationServiceFacade.getProfileAge(any()) } returns Result.success(30L)
        mockkObject(TimeUtils)
        every { TimeUtils.tickerFlow(any()) } returns emptyFlow()
        startKoin {
            modules(
                module {
                    single<NavigationManager> { mockk(relaxed = true) }
                    single<CoroutineJobsManager> { DefaultCoroutineJobsManager() }
                    single<GlobalUiManager> { globalUiManager }
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        try {
            unmockkObject(TimeUtils)
        } catch (_: Exception) {
        }
        try {
            stopKoin()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `onSavePress toggles isBusyWithAction and show hide loading`() =
        runTest(testDispatcher) {
            coEvery {
                userProfileServiceFacade.updateAndPublishUserProfile(any(), any(), any())
            } returns Result.success(profile1)

            val presenter =
                UserProfilePresenter(
                    userProfileServiceFacade = userProfileServiceFacade,
                    reputationServiceFacade = reputationServiceFacade,
                    mainPresenter = mainPresenter,
                )
            presenter.onViewAttached()
            advanceUntilIdle()

            presenter.onAction(UserProfileUiAction.OnSavePress)
            assertTrue(presenter.uiState.value.isBusyWithAction)
            advanceUntilIdle()

            coVerify { userProfileServiceFacade.updateAndPublishUserProfile(any(), any(), any()) }
            assertFalse(presenter.uiState.value.isBusyWithAction)
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
        }
}
