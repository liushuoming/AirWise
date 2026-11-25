package au.edu.jcu.assessment.airwise.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Local Room database. All learning data stays on the device. */
@Database(
    entities = [QuizAttemptEntity::class, LessonCompletionEntity::class, UserSettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AirWiseDatabase : RoomDatabase() {

    abstract fun progressDao(): ProgressDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var instance: AirWiseDatabase? = null

        fun getDatabase(context: Context): AirWiseDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, AirWiseDatabase::class.java, "airwise_database")
                    .build()
                    .also { instance = it }
            }
    }
}
