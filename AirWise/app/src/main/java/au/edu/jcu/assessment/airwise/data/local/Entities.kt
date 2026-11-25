package au.edu.jcu.assessment.airwise.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One finished quiz. [topic] is a Topic name, or null for a mixed quiz. */
@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String?,
    val correct: Int,
    val total: Int,
    val timestamp: Long
)

/** A lesson the learner has marked as complete. */
@Entity(tableName = "lesson_completions")
data class LessonCompletionEntity(
    @PrimaryKey val lessonId: String,
    val completedAt: Long
)

/** The learner's chosen city, stored as a single row (id = 0). */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)
