package au.edu.jcu.assessment.airwise.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.assessment.airwise.data.AirQualityRepository
import au.edu.jcu.assessment.airwise.data.ProgressRepository
import au.edu.jcu.assessment.airwise.data.SettingsRepository
import au.edu.jcu.assessment.airwise.model.LearningStats
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.ui.LiveAirState
import au.edu.jcu.assessment.airwise.ui.loadLive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val location: Location = Location.Default,
    val live: LiveAirState = LiveAirState.Loading,
    val stats: LearningStats = LearningStats()
)

class HomeViewModel(
    private val airQualityRepository: AirQualityRepository,
    private val settingsRepository: SettingsRepository,
    progressRepository: ProgressRepository
) : ViewModel() {

    private val live = MutableStateFlow<LiveAirState>(LiveAirState.Loading)

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.location,
        live,
        progressRepository.stats
    ) { location, liveState, stats ->
        HomeUiState(location, liveState, stats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomeUiState())

    init {
        // Reload whenever the learner picks a different city in Settings.
        viewModelScope.launch {
            settingsRepository.location.distinctUntilChanged().collect { load(it) }
        }
    }

    fun refresh() {
        viewModelScope.launch { load(settingsRepository.location.first()) }
    }

    private suspend fun load(location: Location) {
        live.value = LiveAirState.Loading
        live.value = airQualityRepository.loadLive(location)
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
