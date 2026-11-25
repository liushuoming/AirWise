package au.edu.jcu.assessment.airwise

import au.edu.jcu.assessment.airwise.data.local.LessonCompletionEntity
import au.edu.jcu.assessment.airwise.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.airwise.domain.StatsCalculator
import au.edu.jcu.assessment.airwise.model.Topic
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class StatsCalculatorTest {

    private val calculator = StatsCalculator(ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 11, 24)

    private fun millis(date: LocalDate, hour: Int = 12): Long =
        date.atTime(hour, 0).toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test
    fun streak_countsConsecutiveDaysEndingToday() {
        val days = setOf(today, today.minusDays(1), today.minusDays(2))
        assertEquals(3, calculator.streak(days, today))
    }

    @Test
    fun streak_staysAliveIfLastActivityWasYesterday() {
        val days = setOf(today.minusDays(1), today.minusDays(2))
        assertEquals(2, calculator.streak(days, today))
    }

    @Test
    fun streak_resetsAfterAMissedDay() {
        val days = setOf(today.minusDays(2), today.minusDays(3))
        assertEquals(0, calculator.streak(days, today))
    }

    @Test
    fun percent_roundsAndHandlesZero() {
        assertEquals(67, calculator.percent(2, 3))
        assertEquals(0, calculator.percent(0, 0))
        assertEquals(100, calculator.percent(5, 5))
    }

    @Test
    fun calculate_combinesAttemptsAndCompletions() {
        val attempts = listOf(
            QuizAttemptEntity(1, Topic.PARTICLES.name, correct = 3, total = 5, timestamp = millis(today)),
            QuizAttemptEntity(2, Topic.PARTICLES.name, correct = 5, total = 5, timestamp = millis(today.minusDays(1))),
            QuizAttemptEntity(3, null, correct = 2, total = 5, timestamp = millis(today.minusDays(1)))
        )
        val completions = listOf(LessonCompletionEntity("pm25", millis(today.minusDays(2))))

        val stats = calculator.calculate(attempts, completions, totalLessons = 6, now = millis(today))

        assertEquals(1, stats.lessonsCompleted)
        assertEquals(6, stats.totalLessons)
        assertEquals(3, stats.quizzesTaken)
        assertEquals(15, stats.questionsAnswered)
        assertEquals(10, stats.correctAnswers)
        assertEquals(67, stats.accuracyPercent)
        assertEquals(100, stats.bestScorePercent)
        assertEquals(3, stats.currentStreakDays)
        // Mixed quizzes count overall but not towards a topic.
        assertEquals(mapOf(Topic.PARTICLES to 80), stats.topicAccuracy)
        // Most recent quiz first.
        assertEquals(millis(today), stats.recentAttempts.first().timestamp)
    }

    @Test
    fun calculate_withNoData_returnsZeros() {
        val stats = calculator.calculate(emptyList(), emptyList(), totalLessons = 6, now = millis(today))
        assertEquals(0, stats.accuracyPercent)
        assertEquals(0, stats.currentStreakDays)
        assertEquals(0, stats.quizzesTaken)
    }
}
