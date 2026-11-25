package au.edu.jcu.assessment.airwise

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.assessment.airwise.data.local.AirWiseDatabase
import au.edu.jcu.assessment.airwise.data.local.LessonCompletionEntity
import au.edu.jcu.assessment.airwise.data.local.ProgressDao
import au.edu.jcu.assessment.airwise.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.airwise.data.local.SettingsDao
import au.edu.jcu.assessment.airwise.data.local.UserSettingsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Room tests using an in-memory database, so nothing touches real user data. */
@RunWith(AndroidJUnit4::class)
class ProgressDaoTest {

    private lateinit var database: AirWiseDatabase
    private lateinit var progressDao: ProgressDao
    private lateinit var settingsDao: SettingsDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AirWiseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        progressDao = database.progressDao()
        settingsDao = database.settingsDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun attempts_areReturnedNewestFirst() = runTest {
        progressDao.insertAttempt(QuizAttemptEntity(topic = "GASES", correct = 2, total = 5, timestamp = 100))
        progressDao.insertAttempt(QuizAttemptEntity(topic = null, correct = 4, total = 5, timestamp = 200))

        val attempts = progressDao.observeAttempts().first()
        assertEquals(listOf(200L, 100L), attempts.map { it.timestamp })
    }

    @Test
    fun completingLessonTwice_keepsOneRow() = runTest {
        progressDao.insertCompletion(LessonCompletionEntity("pm25", 100))
        progressDao.insertCompletion(LessonCompletionEntity("pm25", 200))

        val completions = progressDao.observeCompletions().first()
        assertEquals(1, completions.size)
        assertEquals(200L, completions.first().completedAt)
    }

    @Test
    fun deleteAll_removesEveryRow() = runTest {
        progressDao.insertAttempt(QuizAttemptEntity(topic = "HEALTH", correct = 1, total = 3, timestamp = 1))
        progressDao.insertCompletion(LessonCompletionEntity("protect", 1))

        progressDao.deleteAllAttempts()
        progressDao.deleteAllCompletions()

        assertTrue(progressDao.observeAttempts().first().isEmpty())
        assertTrue(progressDao.observeCompletions().first().isEmpty())
    }

    @Test
    fun settings_saveReplaceAndDelete() = runTest {
        assertNull(settingsDao.observeSettings().first())

        settingsDao.saveSettings(UserSettingsEntity(cityName = "Cairns", country = "Australia", latitude = -16.9, longitude = 145.8))
        settingsDao.saveSettings(UserSettingsEntity(cityName = "Sydney", country = "Australia", latitude = -33.9, longitude = 151.2))
        assertEquals("Sydney", settingsDao.observeSettings().first()?.cityName)

        settingsDao.deleteSettings()
        assertNull(settingsDao.observeSettings().first())
    }
}
