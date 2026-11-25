package au.edu.jcu.assessment.airwise.model

/** A place the learner wants live air quality examples for. */
data class Location(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = if (country.isBlank()) name else "$name, $country"

    companion object {
        val Default = Location("Townsville", "Australia", -19.2664, 146.8057)

        val Presets = listOf(
            Default,
            Location("Cairns", "Australia", -16.9203, 145.7710),
            Location("Brisbane", "Australia", -27.4679, 153.0281),
            Location("Sydney", "Australia", -33.8678, 151.2073)
        )
    }
}

/** Current air quality readings for one location. */
data class AirReading(
    val location: Location,
    val updatedAt: String,
    val usAqi: Int?,
    val pm25: Double?,
    val pm10: Double?,
    val ozone: Double?,
    val nitrogenDioxide: Double?
) {
    fun valueOf(pollutant: Pollutant): Double? = when (pollutant) {
        Pollutant.PM25 -> pm25
        Pollutant.PM10 -> pm10
        Pollutant.OZONE -> ozone
        Pollutant.NITROGEN_DIOXIDE -> nitrogenDioxide
    }
}

/** Pollutants the lessons teach about. All values are in µg/m³. */
enum class Pollutant(val label: String) {
    PM25("PM2.5"),
    PM10("PM10"),
    OZONE("Ozone (O₃)"),
    NITROGEN_DIOXIDE("Nitrogen dioxide (NO₂)")
}

/** Lesson topics. Quizzes and statistics are grouped by topic. */
enum class Topic(val title: String) {
    AQI_BASICS("AQI basics"),
    PARTICLES("Particle pollution"),
    GASES("Gas pollutants"),
    HEALTH("Protecting your health")
}

data class LessonSection(val heading: String, val body: String)

data class Lesson(
    val id: String,
    val topic: Topic,
    val title: String,
    val summary: String,
    val minutes: Int,
    val sections: List<LessonSection>,
    /** When set, the lesson shows today's live reading for this pollutant. */
    val livePollutant: Pollutant? = null
)

data class QuizQuestion(
    val id: String,
    val topic: Topic,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    /** True for questions generated from today's live data. */
    val isLive: Boolean = false
) {
    init {
        require(options.size >= 2) { "A question needs at least two options" }
        require(correctIndex in options.indices) { "correctIndex must point to an option" }
    }
}

/** One finished quiz, as shown in the progress history. */
data class AttemptSummary(
    val topic: Topic?,
    val correct: Int,
    val total: Int,
    val timestamp: Long
)

/** Everything the Home and Progress screens show about the learner. */
data class LearningStats(
    val lessonsCompleted: Int = 0,
    val totalLessons: Int = 0,
    val quizzesTaken: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val accuracyPercent: Int = 0,
    val bestScorePercent: Int = 0,
    val currentStreakDays: Int = 0,
    val topicAccuracy: Map<Topic, Int> = emptyMap(),
    val recentAttempts: List<AttemptSummary> = emptyList()
)
