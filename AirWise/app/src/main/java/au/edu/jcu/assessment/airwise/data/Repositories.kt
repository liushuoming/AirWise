package au.edu.jcu.assessment.airwise.data

import au.edu.jcu.assessment.airwise.data.local.LessonCompletionEntity
import au.edu.jcu.assessment.airwise.data.local.ProgressDao
import au.edu.jcu.assessment.airwise.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.airwise.data.local.SettingsDao
import au.edu.jcu.assessment.airwise.data.local.UserSettingsEntity
import au.edu.jcu.assessment.airwise.data.remote.AirQualityApiService
import au.edu.jcu.assessment.airwise.data.remote.GeocodingApiService
import au.edu.jcu.assessment.airwise.domain.StatsCalculator
import au.edu.jcu.assessment.airwise.model.AirReading
import au.edu.jcu.assessment.airwise.model.LearningStats
import au.edu.jcu.assessment.airwise.model.Lesson
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.model.QuizQuestion
import au.edu.jcu.assessment.airwise.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ---------------- Live air quality (network) ----------------

interface AirQualityRepository {
    suspend fun getReading(location: Location): AirReading
    suspend fun findLocation(query: String): Location?
}

class NetworkAirQualityRepository(
    private val airQualityApi: AirQualityApiService,
    private val geocodingApi: GeocodingApiService
) : AirQualityRepository {

    override suspend fun getReading(location: Location): AirReading {
        val current = airQualityApi.getCurrentAirQuality(location.latitude, location.longitude).current
        return AirReading(
            location = location,
            updatedAt = formatTime(current.time),
            usAqi = current.usAqi?.roundToInt(),
            pm25 = current.pm25,
            pm10 = current.pm10,
            ozone = current.ozone,
            nitrogenDioxide = current.nitrogenDioxide
        )
    }

    override suspend fun findLocation(query: String): Location? {
        val result = geocodingApi.searchLocation(query.trim()).results.firstOrNull() ?: return null
        return Location(result.name, result.country, result.latitude, result.longitude)
    }

    /** "2026-11-24T14:00" (local time at the city) -> "2:00 PM, 24 Nov". */
    private fun formatTime(isoTime: String): String = try {
        LocalDateTime.parse(isoTime).format(DateTimeFormatter.ofPattern("h:mm a, d MMM", Locale.getDefault()))
    } catch (e: Exception) {
        isoTime
    }
}

// ---------------- Learning progress (Room) ----------------

interface ProgressRepository {
    val stats: Flow<LearningStats>
    val completedLessonIds: Flow<Set<String>>
    suspend fun recordQuiz(topic: Topic?, correct: Int, total: Int)
    suspend fun markLessonComplete(lessonId: String)
    suspend fun resetProgress()
}

class OfflineProgressRepository(
    private val dao: ProgressDao,
    private val calculator: StatsCalculator,
    private val totalLessons: Int,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ProgressRepository {

    override val stats: Flow<LearningStats> =
        combine(dao.observeAttempts(), dao.observeCompletions()) { attempts, completions ->
            calculator.calculate(attempts, completions, totalLessons, clock())
        }

    override val completedLessonIds: Flow<Set<String>> =
        dao.observeCompletions().map { list -> list.map { it.lessonId }.toSet() }

    override suspend fun recordQuiz(topic: Topic?, correct: Int, total: Int) {
        dao.insertAttempt(QuizAttemptEntity(topic = topic?.name, correct = correct, total = total, timestamp = clock()))
    }

    override suspend fun markLessonComplete(lessonId: String) {
        dao.insertCompletion(LessonCompletionEntity(lessonId, clock()))
    }

    override suspend fun resetProgress() {
        dao.deleteAllAttempts()
        dao.deleteAllCompletions()
    }
}

// ---------------- Settings (Room) ----------------

interface SettingsRepository {
    val location: Flow<Location>
    suspend fun setLocation(location: Location)
    suspend fun clear()
}

class OfflineSettingsRepository(private val dao: SettingsDao) : SettingsRepository {

    override val location: Flow<Location> = dao.observeSettings().map { entity ->
        entity?.let { Location(it.cityName, it.country, it.latitude, it.longitude) } ?: Location.Default
    }

    override suspend fun setLocation(location: Location) {
        dao.saveSettings(
            UserSettingsEntity(
                cityName = location.name,
                country = location.country,
                latitude = location.latitude,
                longitude = location.longitude
            )
        )
    }

    override suspend fun clear() = dao.deleteSettings()
}

// ---------------- Lessons (bundled content) ----------------

interface LessonRepository {
    fun lessons(): List<Lesson>
    fun lesson(id: String): Lesson?
    fun questions(topic: Topic?): List<QuizQuestion>
}

class LocalLessonRepository : LessonRepository {
    override fun lessons(): List<Lesson> = LessonContent.lessons
    override fun lesson(id: String): Lesson? = LessonContent.lessons.firstOrNull { it.id == id }
    override fun questions(topic: Topic?): List<QuizQuestion> =
        LessonContent.questions.filter { topic == null || it.topic == topic }
}
