package au.edu.jcu.assessment.airwise.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.airwise.model.QuizQuestion
import au.edu.jcu.assessment.airwise.model.Topic
import au.edu.jcu.assessment.airwise.ui.theme.AirWiseTheme

@Composable
fun QuizScreen(
    uiState: QuizUiState,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        uiState.questions.isEmpty() -> Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No questions are available for this topic yet.")
        }
        uiState.isFinished -> QuizResult(uiState, onRestart, onFinish, modifier)
        else -> QuestionContent(uiState, onSelectAnswer, onNext, modifier)
    }
}

@Composable
private fun QuestionContent(
    uiState: QuizUiState,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = uiState.currentQuestion ?: return
    val total = uiState.questions.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Question ${uiState.currentIndex + 1} of $total",
            style = MaterialTheme.typography.labelLarge
        )
        LinearProgressIndicator(
            progress = { (uiState.currentIndex + 1) / total.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        if (question.isLive) {
            Text(
                "LIVE DATA QUESTION",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(question.prompt, style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("quiz_prompt"))

        question.options.forEachIndexed { index, option ->
            AnswerOption(
                text = option,
                state = optionState(index, uiState.selectedIndex, question.correctIndex),
                enabled = !uiState.isAnswered,
                onClick = { onSelectAnswer(index) }
            )
        }

        if (uiState.isAnswered) {
            Feedback(question, uiState.selectedIndex == question.correctIndex)
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (uiState.isLastQuestion) "See results" else "Next question")
            }
        }
    }
}

private enum class OptionState { NEUTRAL, CORRECT, WRONG }

private fun optionState(index: Int, selected: Int?, correct: Int): OptionState = when {
    selected == null -> OptionState.NEUTRAL
    index == correct -> OptionState.CORRECT
    index == selected -> OptionState.WRONG
    else -> OptionState.NEUTRAL
}

@Composable
private fun AnswerOption(text: String, state: OptionState, enabled: Boolean, onClick: () -> Unit) {
    val border = when (state) {
        OptionState.CORRECT -> BorderStroke(3.dp, Color(0xFF2E7D32))
        OptionState.WRONG -> BorderStroke(3.dp, Color(0xFFC62828))
        OptionState.NEUTRAL -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }
    // Text suffix so correctness is never shown by colour alone.
    val suffix = when (state) {
        OptionState.CORRECT -> " — correct answer"
        OptionState.WRONG -> " — your answer"
        OptionState.NEUTRAL -> ""
    }
    OutlinedCard(
        onClick = onClick,
        enabled = enabled,
        border = border,
        colors = CardDefaults.outlinedCardColors(disabledContentColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { role = Role.Button }
    ) {
        Text(
            text = text + suffix,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (state == OptionState.NEUTRAL) FontWeight.Normal else FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun Feedback(question: QuizQuestion, isCorrect: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (isCorrect) "Correct!" else "Not quite. The answer is: ${question.options[question.correctIndex]}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun QuizResult(uiState: QuizUiState, onRestart: () -> Unit, onFinish: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quiz complete", style = MaterialTheme.typography.headlineSmall)
        Text(
            "You scored ${uiState.score} out of ${uiState.questions.size} (${uiState.scorePercent}%)",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Your result is saved on this device only.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Try another quiz") }
        OutlinedButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizScreenPreview() {
    val question = QuizQuestion(
        id = "preview",
        topic = Topic.AQI_BASICS,
        prompt = "Right now the US AQI in Townsville is 42. Which category is this?",
        options = listOf("Moderate", "Good", "Unhealthy", "Hazardous"),
        correctIndex = 1,
        explanation = "An AQI of 42 is in the Good range.",
        isLive = true
    )
    AirWiseTheme {
        QuizScreen(
            uiState = QuizUiState(isLoading = false, questions = listOf(question), selectedIndex = 0),
            onSelectAnswer = {},
            onNext = {},
            onRestart = {},
            onFinish = {}
        )
    }
}
