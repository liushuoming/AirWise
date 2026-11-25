package au.edu.jcu.assessment.airwise.ui

import au.edu.jcu.assessment.airwise.data.AirQualityRepository
import au.edu.jcu.assessment.airwise.model.AirReading
import au.edu.jcu.assessment.airwise.model.Location
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** State of a live air quality request, shared by several screens. */
sealed interface LiveAirState {
    data object Loading : LiveAirState
    data class Success(val reading: AirReading) : LiveAirState
    data class Error(val message: String) : LiveAirState
}

/** Fetches a reading and turns network failures into a friendly error state. */
suspend fun AirQualityRepository.loadLive(location: Location): LiveAirState = try {
    LiveAirState.Success(getReading(location))
} catch (e: IOException) {
    LiveAirState.Error("No internet connection. Lessons and quizzes still work offline.")
} catch (e: HttpException) {
    LiveAirState.Error("Live data is unavailable right now (error ${e.code()}).")
} catch (e: SerializationException) {
    LiveAirState.Error("Received unexpected data from the air quality service.")
}
