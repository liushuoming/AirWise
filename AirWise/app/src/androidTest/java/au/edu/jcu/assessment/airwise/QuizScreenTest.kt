package au.edu.jcu.assessment.airwise

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.assessment.airwise.model.QuizQuestion
import au.edu.jcu.assessment.airwise.model.Topic
import au.edu.jcu.assessment.airwise.ui.quiz.QuizScreen
import au.edu.jcu.assessment.airwise.ui.quiz.QuizUiState
import au.edu.jcu.assessment.airwise.ui.theme.AirWiseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** GUI tests for the quiz screen, driven by hoisted state like the real ViewModel. */
@RunWith(AndroidJUnit4::class)
class QuizScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val questions = listOf(
        QuizQuestion("q1", Topic.AQI_BASICS, "What does a higher AQI value mean?",
            listOf("Cleaner air", "More polluted air"), 1, "Higher AQI means more pollution."),
        QuizQuestion("q2", Topic.AQI_BASICS, "Which AQI range is Moderate?",
            listOf("0–50", "51–100"), 1, "Moderate is 51–100.")
    )

    private fun setQuiz(initial: QuizUiState = QuizUiState(isLoading = false, questions = questions)) {
        composeTestRule.setContent {
            var state by remember { mutableStateOf(initial) }
            AirWiseTheme {
                QuizScreen(
                    uiState = state,
                    onSelectAnswer = { index ->
                        val q = state.currentQuestion!!
                        state = state.copy(
                            selectedIndex = index,
                            score = state.score + if (index == q.correctIndex) 1 else 0
                        )
                    },
                    onNext = {
                        state = if (state.isLastQuestion) state.copy(isFinished = true)
                        else state.copy(currentIndex = state.currentIndex + 1, selectedIndex = null)
                    },
                    onRestart = {},
                    onFinish = {}
                )
            }
        }
    }

    @Test
    fun showsFirstQuestionAndProgress() {
        setQuiz()
        composeTestRule.onNodeWithTag("quiz_prompt").assertIsDisplayed()
        composeTestRule.onNodeWithText("What does a higher AQI value mean?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Question 1 of 2").assertIsDisplayed()
    }

    @Test
    fun correctAnswer_showsCorrectFeedbackInText() {
        setQuiz()
        composeTestRule.onNodeWithText("More polluted air").performClick()
        composeTestRule.onNodeWithText("Correct!").assertIsDisplayed()
        composeTestRule.onNodeWithText("More polluted air — correct answer").assertIsDisplayed()
    }

    @Test
    fun wrongAnswer_explainsTheRightAnswer() {
        setQuiz()
        composeTestRule.onNodeWithText("Cleaner air").performClick()
        composeTestRule.onNodeWithText("Not quite. The answer is: More polluted air").assertIsDisplayed()
    }

    @Test
    fun answeringAll_showsResultScreen() {
        setQuiz()
        composeTestRule.onNodeWithText("More polluted air").performClick()
        composeTestRule.onNodeWithText("Next question").performScrollTo().performClick()
        composeTestRule.onNodeWithText("51–100").performClick()
        composeTestRule.onNodeWithText("See results").performScrollTo().performClick()
        composeTestRule.onNodeWithText("You scored 2 out of 2 (100%)").assertIsDisplayed()
    }

    @Test
    fun liveQuestion_isLabelled() {
        val live = questions.first().copy(id = "live", isLive = true)
        setQuiz(QuizUiState(isLoading = false, questions = listOf(live)))
        composeTestRule.onNodeWithText("LIVE DATA QUESTION").assertIsDisplayed()
    }
}
