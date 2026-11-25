package au.edu.jcu.assessment.airwise.domain

import au.edu.jcu.assessment.airwise.data.local.LessonCompletionEntity
import au.edu.jcu.assessment.airwise.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.airwise.model.AttemptSummary
import au.edu.jcu.assessment.airwise.model.LearningStats
import au.edu.jcu.assessment.airwise.model.Topic
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

/** Turns stored quiz attempts and lesson completions into learning statistics. */
class StatsCalculator(private val zone: ZoneId = ZoneId.systemDefault()) {

    fun calculate(
        attempts: List<QuizAttemptEntity>,
        completions: List<LessonCompletionEntity>,
        totalLessons: Int,
        now: Long
    ): LearningStats {
        val answered = attempts.sumOf { it.total }
        val correct = attempts.sumOf { it.correct }

        val topicAccuracy = attempts
            .mapNotNull { attempt -> topicOf(attempt.topic)?.let { it to attempt } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, list) -> percent(list.sumOf { it.correct }, list.sumOf { it.total }) }

        val activityDays = (attempts.map { it.timestamp } + completions.map { it.completedAt })
            .map { toDate(it) }
            .toSet()

        return LearningStats(
            lessonsCompleted = completions.size.coerceAtMost(totalLessons),
            totalLessons = totalLessons,
            quizzesTaken = attempts.size,
            questionsAnswered = answered,
            correctAnswers = correct,
            accuracyPercent = percent(correct, answered),
            bestScorePercent = attempts.maxOfOrNull { percent(it.correct, it.total) } ?: 0,
            currentStreakDays = streak(activityDays, toDate(now)),
            topicAccuracy = topicAccuracy,
            recentAttempts = attempts
                .sortedByDescending { it.timestamp }
                .take(RECENT_LIMIT)
                .map { AttemptSummary(topicOf(it.topic), it.correct, it.total, it.timestamp) }
        )
    }

    /**
     * Counts consecutive days of learning. A streak stays alive until the end
     * of today, so it counts back from yesterday if nothing is done yet today.
     */
    fun streak(activityDays: Set<LocalDate>, today: LocalDate): Int {
        var day = if (today in activityDays) today else today.minusDays(1)
        var count = 0
        while (day in activityDays) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    fun percent(correct: Int, total: Int): Int =
        if (total <= 0) 0 else (correct * 100.0 / total).roundToInt()

    private fun toDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    private fun topicOf(name: String?): Topic? = Topic.entries.firstOrNull { it.name == name }

    companion object {
        const val RECENT_LIMIT = 5
    }
}
