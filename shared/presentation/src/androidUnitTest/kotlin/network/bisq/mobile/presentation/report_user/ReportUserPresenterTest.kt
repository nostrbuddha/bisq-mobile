package network.bisq.mobile.presentation.report_user

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import network.bisq.mobile.data.replicated.chat.ChatMessageTypeEnum
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessageDto
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessageModel
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.domain.utils.CoroutineJobsManager
import network.bisq.mobile.domain.utils.DefaultCoroutineJobsManager
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.common.ui.navigation.manager.NavigationManager
import network.bisq.mobile.presentation.main.MainPresenter
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ReportUserPresenterTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var globalUiManager: GlobalUiManager

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        I18nSupport.initialize("en")
        globalUiManager = mockk(relaxed = true)
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
            stopKoin()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun createChatMessage(): BisqEasyOpenTradeMessageModel {
        val myUserProfile = createMockUserProfile("me")
        val sender = createMockUserProfile("them")
        val dto = mockk<BisqEasyOpenTradeMessageDto>()
        every { dto.chatMessageType } returns ChatMessageTypeEnum.TEXT
        every { dto.senderUserProfile } returns sender
        every { dto.messageId } returns "m1"
        every { dto.text } returns "bad msg"
        every { dto.citation } returns null
        every { dto.date } returns 1L
        every { dto.tradeId } returns "t1"
        every { dto.mediator } returns null
        every { dto.bisqEasyOffer } returns null
        every { dto.citationAuthorUserProfile } returns null
        return BisqEasyOpenTradeMessageModel(dto, myUserProfile, emptyList())
    }

    @Test
    fun `onReportClick calls service with sender and message emits success and clears loading`() =
        runTest(testDispatcher) {
            val userProfileServiceFacade = mockk<UserProfileServiceFacade>(relaxed = true)
            val mainPresenter = mockk<MainPresenter>(relaxed = true)
            coEvery {
                userProfileServiceFacade.reportUserProfile(any(), any())
            } returns Result.success(Unit)

            val chatMessage = createChatMessage()
            val reportText = "report text"

            val presenter = ReportUserPresenter(mainPresenter, userProfileServiceFacade)
            val effects = mutableListOf<ReportUserEffect>()
            val collectJob = launch { presenter.effect.collect { effects.add(it) } }
            advanceUntilIdle() // register collector on SharedFlow before any emit
            presenter.initialize(chatMessage, reportText)

            presenter.onReportClick()
            advanceUntilIdle()
            collectJob.cancel()

            assertEquals(1, effects.size)
            assertEquals(ReportUserEffect.ReportSuccess, effects.single())
            assertFalse(presenter.uiState.value.isLoading)
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
            val sentProfile = slot<UserProfileVO>()
            coVerify(exactly = 1) { userProfileServiceFacade.reportUserProfile(capture(sentProfile), reportText) }
            assertEquals(chatMessage.senderUserProfile, sentProfile.captured)
        }

    @Test
    fun `onReportClick calls service emits error effect on failure and clears loading`() =
        runTest(testDispatcher) {
            val userProfileServiceFacade = mockk<UserProfileServiceFacade>(relaxed = true)
            val mainPresenter = mockk<MainPresenter>(relaxed = true)
            coEvery {
                userProfileServiceFacade.reportUserProfile(any(), any())
            } returns Result.failure(Exception("network"))

            val chatMessage = createChatMessage()
            val reportText = "report text"
            val expectedErrorMessage = "mobile.chat.reportToModerator.error".i18n()

            val presenter = ReportUserPresenter(mainPresenter, userProfileServiceFacade)
            val effects = mutableListOf<ReportUserEffect>()
            val collectJob = launch { presenter.effect.collect { effects.add(it) } }
            advanceUntilIdle() // register collector on SharedFlow before any emit
            presenter.initialize(chatMessage, reportText)

            presenter.onReportClick()
            advanceUntilIdle()
            collectJob.cancel()

            val err = assertIs<ReportUserEffect.ReportError>(effects.single())
            assertEquals(expectedErrorMessage, err.message)
            assertEquals(reportText, err.reportMessage)
            assertFalse(presenter.uiState.value.isLoading)
            verify(exactly = 1) { globalUiManager.scheduleShowLoading() }
            verify(exactly = 1) { globalUiManager.hideLoading() }
            val sentProfile = slot<UserProfileVO>()
            coVerify(exactly = 1) { userProfileServiceFacade.reportUserProfile(capture(sentProfile), reportText) }
            assertEquals(chatMessage.senderUserProfile, sentProfile.captured)
        }
}
