package au.edu.jcu.assessment.airwise.domain

import au.edu.jcu.assessment.airwise.model.AirReading
import au.edu.jcu.assessment.airwise.model.AqiCategory
import au.edu.jcu.assessment.airwise.model.QuizQuestion
import au.edu.jcu.assessment.airwise.model.Topic
import kotlin.random.Random

/**
 * Builds a quiz from the question bank. When live data is available, the quiz
 * starts with a question about today's real AQI, linking the lesson to the
 * learner's own city. Pure Kotlin, so it is easy to unit test.
 */
class QuizGenerator(private val random: Random = Random.Default) {

    /**
     * @param topic only use questions from this topic, or null for a mixed quiz
     * @param bank all available questions
     * @param liveReading today's reading, or null when offline
     * @param size the maximum number of questions
     */
    fun buildQuiz(
        topic: Topic?,
        bank: List<QuizQuestion>,
        liveReading: AirReading?,
        size: Int = DEFAULT_SIZE
    ): List<QuizQuestion> {
        require(size > 0) { "Quiz size must be positive" }

        val pool = bank.filter { topic == null || it.topic == topic }.shuffled(random)

        // The live question is about AQI categories, so it fits mixed and AQI quizzes.
        val liveQuestion = if (topic == null || topic == Topic.AQI_BASICS) {
            liveReading?.let { liveCategoryQuestion(it) }
        } else {
            null
        }

        return if (liveQuestion != null) {
            listOf(liveQuestion) + pool.take(size - 1)
        } else {
            pool.take(size)
        }
    }

    /** "Right now the AQI in Townsville is 42. Which category is this?" */
    fun liveCategoryQuestion(reading: AirReading): QuizQuestion? {
        val aqi = reading.usAqi ?: return null
        val correct = AqiCategory.from(aqi)
        val distractors = AqiCategory.entries.filter { it != correct }.shuffled(random).take(3)
        val options = (distractors + correct).shuffled(random)

        return QuizQuestion(
            id = LIVE_QUESTION_ID,
            topic = Topic.AQI_BASICS,
            prompt = "Right now the US AQI in ${reading.location.name} is $aqi. Which category is this?",
            options = options.map { it.label },
            correctIndex = options.indexOf(correct),
            explanation = "An AQI of $aqi is in the ${correct.label} range. ${correct.advice}",
            isLive = true
        )
    }

    companion object {
        const val DEFAULT_SIZE = 5
        const val LIVE_QUESTION_ID = "live-aqi"
    }
}
