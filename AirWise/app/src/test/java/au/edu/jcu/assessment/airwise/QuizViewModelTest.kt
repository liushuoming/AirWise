package au.edu.jcu.assessment.airwise

import androidx.lifecycle.SavedStateHandle
import au.edu.jcu.assessment.airwise.data.LocalLessonRepository
import au.edu.jcu.assessment.airwise.domain.QuizGenerator
import au.edu.jcu.assessment.airwise.fake.FakeAirQualityRepository
import au.edu.jcu.assessment.airwise.fake.FakeProgressRepository
import au.edu.jcu.assessment.airwise.fake.FakeSettingsRepository
import au.edu.jcu.assessment.airwise.fake.RecordedQuiz
import au.edu.jcu.assessment.airwise.model.Topic
import au.edu.jcu.assessment.airwise.ui.navigation.QuizDestination
import au.edu.jcu.assessment.airwise.ui.quiz.QuizViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val progress = FakeProgressRepository()
    private val air = FakeAirQualityRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(topicArg: String) = QuizViewModel(
        savedStateHandle = SavedStateHandle(mapOf(QuizDestination.TOPIC_ARG to topicArg)),
        lessonRepository = LocalLessonRepository(),
        progressRepository = progress,
        airQualityRepository = air,
        settingsRepository = FakeSettingsRepository(),
        generator = QuizGenerator(Random(1))
    )

    @Test
    fun topicArgument_isParsed() {
        assertEquals(Topic.GASES, createViewModel(Topic.GASES.name).topic)
        assertNull(createViewModel(QuizDestination.MIXED).topic)
    }

    @Test
    fun quizLoads_withLiveQuestionWhenOnline() {
        val state = createViewModel(QuizDestination.MIXED).uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.questions.first().isLive)
    }

    @Test
    fun quizStillLoads_whenOffline() {
        air.offline = true
        val state = createViewModel(QuizDestination.MIXED).uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.questions.isNotEmpty())
        assertFalse(state.questions.any { it.isLive })
    }

    @Test
    fun correctAnswer_increasesScore_andCanOnlyBeAnsweredOnce() {
        val viewModel = createViewModel(Topic.PARTICLES.name)
        val question = viewModel.uiState.value.currentQuestion!!

        viewModel.selectAnswer(question.correctIndex)
        assertEquals(1, viewModel.uiState.value.score)

        // A second tap must not change the answer or the score.
        val wrong = (question.correctIndex + 1) % question.options.size
        viewModel.selectAnswer(wrong)
        assertEquals(question.correctIndex, viewModel.uiState.value.selectedIndex)
        assertEquals(1, viewModel.uiState.value.score)
    }

    @Test
    fun cannotMoveOn_beforeAnswering() {
        val viewModel = createViewModel(Topic.PARTICLES.name)
        viewModel.nextQuestion()
        assertEquals(0, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun finishingQuiz_savesResultToProgress() {
        val viewModel = createViewModel(Topic.HEALTH.name)
        val total = viewModel.uiState.value.questions.size

        repeat(total) {
            val question = viewModel.uiState.value.currentQuestion!!
            viewModel.selectAnswer(question.correctIndex)
            viewModel.nextQuestion()
        }

        assertTrue(viewModel.uiState.value.isFinished)
        assertEquals(100, viewModel.uiState.value.scorePercent)
        assertEquals(listOf(RecordedQuiz(Topic.HEALTH, total, total)), progress.recordedQuizzes)
    }
}
