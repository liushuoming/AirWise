package au.edu.jcu.assessment.airwise

import au.edu.jcu.assessment.airwise.data.LessonContent
import au.edu.jcu.assessment.airwise.domain.QuizGenerator
import au.edu.jcu.assessment.airwise.model.AirReading
import au.edu.jcu.assessment.airwise.model.AqiCategory
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizGeneratorTest {

    private val generator = QuizGenerator(Random(42))
    private val bank = LessonContent.questions

    private fun reading(aqi: Int?) = AirReading(Location.Default, "now", aqi, 5.0, 10.0, 40.0, 3.0)

    @Test
    fun topicQuiz_onlyContainsQuestionsFromThatTopic() {
        val quiz = generator.buildQuiz(Topic.PARTICLES, bank, liveReading = null)
        assertTrue(quiz.isNotEmpty())
        assertTrue(quiz.all { it.topic == Topic.PARTICLES })
    }

    @Test
    fun mixedQuiz_withLiveData_startsWithLiveQuestion() {
        val quiz = generator.buildQuiz(null, bank, reading(42))
        assertTrue(quiz.first().isLive)
        assertEquals(QuizGenerator.DEFAULT_SIZE, quiz.size)
    }

    @Test
    fun offlineQuiz_hasNoLiveQuestion() {
        val quiz = generator.buildQuiz(null, bank, liveReading = null)
        assertFalse(quiz.any { it.isLive })
        assertEquals(QuizGenerator.DEFAULT_SIZE, quiz.size)
    }

    @Test
    fun liveQuestion_notAddedToUnrelatedTopic() {
        val quiz = generator.buildQuiz(Topic.GASES, bank, reading(42))
        assertFalse(quiz.any { it.isLive })
    }

    @Test
    fun liveQuestion_correctAnswerMatchesAqiCategory() {
        listOf(10, 75, 120, 180, 250, 400).forEach { aqi ->
            val question = generator.liveCategoryQuestion(reading(aqi))!!
            assertEquals(AqiCategory.from(aqi).label, question.options[question.correctIndex])
        }
    }

    @Test
    fun liveQuestion_hasFourDistinctOptions() {
        val question = generator.liveCategoryQuestion(reading(42))!!
        assertEquals(4, question.options.size)
        assertEquals(4, question.options.toSet().size)
    }

    @Test
    fun liveQuestion_isNullWithoutAqi() {
        assertNull(generator.liveCategoryQuestion(reading(null)))
    }

    @Test
    fun quiz_hasNoDuplicateQuestions() {
        val quiz = generator.buildQuiz(null, bank, reading(42), size = 10)
        assertEquals(quiz.size, quiz.map { it.id }.toSet().size)
    }

    @Test
    fun quiz_neverLargerThanAvailableQuestions() {
        val quiz = generator.buildQuiz(Topic.HEALTH, bank, liveReading = null, size = 50)
        assertEquals(bank.count { it.topic == Topic.HEALTH }, quiz.size)
    }
}
