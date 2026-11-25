package au.edu.jcu.assessment.airwise.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.assessment.airwise.data.AirQualityRepository
import au.edu.jcu.assessment.airwise.data.LessonRepository
import au.edu.jcu.assessment.airwise.data.ProgressRepository
import au.edu.jcu.assessment.airwise.data.SettingsRepository
import au.edu.jcu.assessment.airwise.domain.QuizGenerator
import au.edu.jcu.assessment.airwise.model.QuizQuestion
import au.edu.jcu.assessment.airwise.model.Topic
import au.edu.jcu.assessment.airwise.ui.LiveAirState
import au.edu.jcu.assessment.airwise.ui.loadLive
import au.edu.jcu.assessment.airwise.ui.navigation.QuizDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val topic: Topic? = null,
    val isLoading: Boolean = true,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedIndex: Int? = null,
    val score: Int = 0,
    val isFinished: Boolean = false
) {
    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)
    val isAnswered: Boolean get() = selectedIndex != null
    val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
    val scorePercent: Int get() = if (questions.isEmpty()) 0 else score * 100 / questions.size
}

class QuizViewModel(
    savedStateHandle: SavedStateHandle,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val airQualityRepository: AirQualityRepository,
    private val settingsRepository: SettingsRepository,
    private val generator: QuizGenerator
) : ViewModel() {

    /** Null means a mixed quiz across all topics. */
    val topic: Topic? = savedStateHandle.get<String>(QuizDestination.TOPIC_ARG)
        ?.let { arg -> Topic.entries.firstOrNull { it.name == arg } }

    private val _uiState = MutableStateFlow(QuizUiState(topic = topic))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        startQuiz()
    }

    /** Builds a new quiz. Uses live data when available; works offline without it. */
    fun startQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState(topic = topic, isLoading = true)
            val live = airQualityRepository.loadLive(settingsRepository.location.first())
            val reading = (live as? LiveAirState.Success)?.reading
            val questions = generator.buildQuiz(topic, lessonRepository.questions(topic), reading)
            _uiState.value = QuizUiState(topic = topic, isLoading = false, questions = questions)
        }
    }

    /** Locks in an answer. A question can only be answered once. */
    fun selectAnswer(index: Int) {
        _uiState.update { state ->
            val question = state.currentQuestion
            if (question == null || state.isAnswered || index !in question.options.indices) {
                state
            } else {
                val gained = if (index == question.correctIndex) 1 else 0
                state.copy(selectedIndex = index, score = state.score + gained)
            }
        }
    }

    /** Moves on, or finishes the quiz and saves the result to Room. */
    fun nextQuestion() {
        val state = _uiState.value
        if (!state.isAnswered) return
        if (state.isLastQuestion) {
            _uiState.value = state.copy(isFinished = true)
            viewModelScope.launch {
                progressRepository.recordQuiz(topic, state.score, state.questions.size)
            }
        } else {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1, selectedIndex = null)
        }
    }
}
