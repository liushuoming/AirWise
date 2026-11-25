package au.edu.jcu.assessment.airwise.di

import android.content.Context
import au.edu.jcu.assessment.airwise.data.AirQualityRepository
import au.edu.jcu.assessment.airwise.data.LessonRepository
import au.edu.jcu.assessment.airwise.data.LocalLessonRepository
import au.edu.jcu.assessment.airwise.data.NetworkAirQualityRepository
import au.edu.jcu.assessment.airwise.data.OfflineProgressRepository
import au.edu.jcu.assessment.airwise.data.OfflineSettingsRepository
import au.edu.jcu.assessment.airwise.data.ProgressRepository
import au.edu.jcu.assessment.airwise.data.SettingsRepository
import au.edu.jcu.assessment.airwise.data.local.AirWiseDatabase
import au.edu.jcu.assessment.airwise.data.remote.AirQualityApiService
import au.edu.jcu.assessment.airwise.data.remote.GeocodingApiService
import au.edu.jcu.assessment.airwise.domain.StatsCalculator
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Manual dependency injection: the app's single source of dependencies. */
interface AppContainer {
    val airQualityRepository: AirQualityRepository
    val progressRepository: ProgressRepository
    val settingsRepository: SettingsRepository
    val lessonRepository: LessonRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val json = Json { ignoreUnknownKeys = true }

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val database: AirWiseDatabase by lazy { AirWiseDatabase.getDatabase(context) }

    override val airQualityRepository: AirQualityRepository by lazy {
        NetworkAirQualityRepository(
            retrofit(AirQualityApiService.BASE_URL).create(AirQualityApiService::class.java),
            retrofit(GeocodingApiService.BASE_URL).create(GeocodingApiService::class.java)
        )
    }

    override val lessonRepository: LessonRepository by lazy { LocalLessonRepository() }

    override val progressRepository: ProgressRepository by lazy {
        OfflineProgressRepository(
            dao = database.progressDao(),
            calculator = StatsCalculator(),
            totalLessons = lessonRepository.lessons().size
        )
    }

    override val settingsRepository: SettingsRepository by lazy {
        OfflineSettingsRepository(database.settingsDao())
    }
}
