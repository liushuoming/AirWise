package au.edu.jcu.assessment.airwise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Insert
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun observeAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: LessonCompletionEntity)

    @Query("SELECT * FROM lesson_completions")
    fun observeCompletions(): Flow<List<LessonCompletionEntity>>

    @Query("DELETE FROM quiz_attempts")
    suspend fun deleteAllAttempts()

    @Query("DELETE FROM lesson_completions")
    suspend fun deleteAllCompletions()
}

@Dao
interface SettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 0")
    fun observeSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettingsEntity)

    @Query("DELETE FROM user_settings")
    suspend fun deleteSettings()
}
