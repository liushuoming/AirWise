package au.edu.jcu.assessment.airwise.model

import androidx.compose.ui.graphics.Color

/**
 * US EPA AQI categories. Every category has a text label as well as a colour,
 * so meaning is never carried by colour alone (accessibility).
 */
enum class AqiCategory(val label: String, val color: Color, val advice: String) {
    GOOD("Good", Color(0xFF3E9B4F), "Air quality is satisfactory. Enjoy outdoor activities."),
    MODERATE("Moderate", Color(0xFFE8C547), "Unusually sensitive people should consider reducing long outdoor exertion."),
    SENSITIVE("Unhealthy for sensitive groups", Color(0xFFEE8B33), "People with asthma, children and older adults should reduce long outdoor exertion."),
    UNHEALTHY("Unhealthy", Color(0xFFD64545), "Everyone should reduce long or heavy outdoor exertion."),
    VERY_UNHEALTHY("Very unhealthy", Color(0xFF8E4AA8), "Avoid outdoor exertion. Keep windows closed."),
    HAZARDOUS("Hazardous", Color(0xFF7E0023), "Stay indoors and avoid all outdoor activity.");

    companion object {
        fun from(aqi: Int): AqiCategory = when {
            aqi <= 50 -> GOOD
            aqi <= 100 -> MODERATE
            aqi <= 150 -> SENSITIVE
            aqi <= 200 -> UNHEALTHY
            aqi <= 300 -> VERY_UNHEALTHY
            else -> HAZARDOUS
        }
    }
}
