package au.edu.jcu.assessment.airwise.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo Air Quality API. Free, no API key and no user account, so the app
 * sends nothing about the user except the coordinates of the chosen city.
 * Data licence: CC BY 4.0 (attribution shown in Settings).
 */
interface AirQualityApiService {
    @GET("v1/air-quality")
    suspend fun getCurrentAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") fields: String = CURRENT_FIELDS,
        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponse

    companion object {
        const val BASE_URL = "https://air-quality-api.open-meteo.com/"
        const val CURRENT_FIELDS = "us_aqi,pm2_5,pm10,ozone,nitrogen_dioxide"
    }
}

/** Open-Meteo Geocoding API, used to search for a city by name. */
interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "en"
    ): GeocodingResponse

    companion object {
        const val BASE_URL = "https://geocoding-api.open-meteo.com/"
    }
}

@Serializable
data class AirQualityResponse(val current: CurrentAirQuality)

@Serializable
data class CurrentAirQuality(
    val time: String,
    // Doubles so both "42" and "42.0" in the JSON parse.
    @SerialName("us_aqi") val usAqi: Double? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    val pm10: Double? = null,
    val ozone: Double? = null,
    @SerialName("nitrogen_dioxide") val nitrogenDioxide: Double? = null
)

@Serializable
data class GeocodingResponse(
    // "results" is missing from the JSON when nothing matches.
    val results: List<GeocodingResult> = emptyList()
)

@Serializable
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = ""
)
