package au.edu.jcu.assessment.airwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.assessment.airwise.data.AirQualityRepository
import au.edu.jcu.assessment.airwise.data.ProgressRepository
import au.edu.jcu.assessment.airwise.data.SettingsRepository
import au.edu.jcu.assessment.airwise.model.LearningStats
import au.edu.jcu.assessment.airwise.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

private const val STOP_TIMEOUT_MILLIS = 5_000L

// ---------------- Progress ----------------

class ProgressViewModel(progressRepository: ProgressRepository) : ViewModel() {
    val uiState: StateFlow<LearningStats> = progressRepository.stats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LearningStats())
}

// ---------------- Settings ----------------

data class SettingsUiState(
    val location: Location = Location.Default,
    val isSearching: Boolean = false,
    val message: String? = null
)

private data class SearchState(val isSearching: Boolean = false, val message: String? = null)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val airQualityRepository: AirQualityRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val search = MutableStateFlow(SearchState())

    val uiState: StateFlow<SettingsUiState> = combine(settingsRepository.location, search) { location, s ->
        SettingsUiState(location, s.isSearching, s.message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SettingsUiState())

    fun selectLocation(location: Location) {
        viewModelScope.launch {
            settingsRepository.setLocation(location)
            search.value = SearchState(message = "Live examples now use ${location.displayName}.")
        }
    }

    fun searchCity(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            search.value = SearchState(isSearching = true)
            val message = try {
                val found = airQualityRepository.findLocation(query)
                if (found == null) {
                    "No place called \"$query\" was found."
                } else {
                    settingsRepository.setLocation(found)
                    "Live examples now use ${found.displayName}."
                }
            } catch (e: IOException) {
                "Search failed: no internet connection."
            } catch (e: HttpException) {
                "Search failed (error ${e.code()})."
            }
            search.value = SearchState(message = message)
        }
    }

    /** Deletes every piece of stored learning data (user control over their data). */
    fun deleteAllData() {
        viewModelScope.launch {
            progressRepository.resetProgress()
            settingsRepository.clear()
            search.value = SearchState(message = "All learning data was deleted from this device.")
        }
    }
}
