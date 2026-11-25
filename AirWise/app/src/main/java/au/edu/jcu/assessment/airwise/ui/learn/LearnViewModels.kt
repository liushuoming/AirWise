package au.edu.jcu.assessment.airwise.ui.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.assessment.airwise.data.AirQualityRepository
import au.edu.jcu.assessment.airwise.data.LessonRepository
import au.edu.jcu.assessment.airwise.data.ProgressRepository
import au.edu.jcu.assessment.airwise.data.SettingsRepository
import au.edu.jcu.assessment.airwise.model.Lesson
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.ui.LiveAirState
import au.edu.jcu.assessment.airwise.ui.loadLive
import au.edu.jcu.assessment.airwise.ui.navigation.LessonDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

// ---------------- Lesson list ----------------

data class LearnUiState(
    val lessons: List<Lesson> = emptyList(),
    val completedIds: Set<String> = emptySet()
)

class LearnViewModel(
    lessonRepository: LessonRepository,
    progressRepository: ProgressRepository
) : ViewModel() {

    private val lessons = lessonRepository.lessons()

    val uiState: StateFlow<LearnUiState> = progressRepository.completedLessonIds
        .map { LearnUiState(lessons, it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LearnUiState(lessons))
}

// ---------------- Single lesson ----------------

data class LessonUiState(
    val lesson: Lesson? = null,
    val isCompleted: Boolean = false,
    val location: Location = Location.Default,
    /** Null when the lesson has no live example. */
    val live: LiveAirState? = null
)

class LessonViewModel(
    savedStateHandle: SavedStateHandle,
    lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val airQualityRepository: AirQualityRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle[LessonDestination.LESSON_ID_ARG])
    private val lesson: Lesson? = lessonRepository.lesson(lessonId)
    private val live = MutableStateFlow<LiveAirState?>(null)

    val uiState: StateFlow<LessonUiState> = combine(
        progressRepository.completedLessonIds,
        settingsRepository.location,
        live
    ) { completed, location, liveState ->
        LessonUiState(lesson, lessonId in completed, location, liveState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LessonUiState(lesson))

    init {
        if (lesson?.livePollutant != null) loadLiveExample()
    }

    fun loadLiveExample() {
        viewModelScope.launch {
            live.value = LiveAirState.Loading
            live.value = airQualityRepository.loadLive(settingsRepository.location.first())
        }
    }

    fun markComplete() {
        viewModelScope.launch { progressRepository.markLessonComplete(lessonId) }
    }
}
