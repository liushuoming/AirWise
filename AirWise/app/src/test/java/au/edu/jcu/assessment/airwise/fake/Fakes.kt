package au.edu.jcu.assessment.airwise.fake

import au.edu.jcu.assessment.airwise.data.AirQualityRepository
import au.edu.jcu.assessment.airwise.data.ProgressRepository
import au.edu.jcu.assessment.airwise.data.SettingsRepository
import au.edu.jcu.assessment.airwise.model.AirReading
import au.edu.jcu.assessment.airwise.model.LearningStats
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/** Returns a fixed reading, or throws IOException when [offline] is true. */
class FakeAirQualityRepository(
    private val aqi: Int? = 42,
    var offline: Boolean = false
) : AirQualityRepository {
    override suspend fun getReading(location: Location): AirReading {
        if (offline) throw IOException("offline")
        return AirReading(location, "2:00 PM, 24 Nov", aqi, 6.3, 14.8, 61.0, 3.2)
    }

    override suspend fun findLocation(query: String): Location? {
        if (offline) throw IOException("offline")
        return Location.Presets.firstOrNull { it.name.equals(query, ignoreCase = true) }
    }
}

data class RecordedQuiz(val topic: Topic?, val correct: Int, val total: Int)

class FakeProgressRepository : ProgressRepository {
    val recordedQuizzes = mutableListOf<RecordedQuiz>()
    private val completed = MutableStateFlow<Set<String>>(emptySet())

    override val stats: Flow<LearningStats> = MutableStateFlow(LearningStats())
    override val completedLessonIds: Flow<Set<String>> = completed

    override suspend fun recordQuiz(topic: Topic?, correct: Int, total: Int) {
        recordedQuizzes += RecordedQuiz(topic, correct, total)
    }

    override suspend fun markLessonComplete(lessonId: String) {
        completed.value = completed.value + lessonId
    }

    override suspend fun resetProgress() {
        recordedQuizzes.clear()
        completed.value = emptySet()
    }
}

class FakeSettingsRepository : SettingsRepository {
    private val current = MutableStateFlow(Location.Default)
    override val location: Flow<Location> = current
    override suspend fun setLocation(location: Location) {
        current.value = location
    }
    override suspend fun clear() {
        current.value = Location.Default
    }
}
