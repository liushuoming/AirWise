package au.edu.jcu.assessment.airwise

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.assessment.airwise.data.LessonContent
import au.edu.jcu.assessment.airwise.model.AirReading
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.ui.LiveAirState
import au.edu.jcu.assessment.airwise.ui.home.HomeScreen
import au.edu.jcu.assessment.airwise.ui.home.HomeUiState
import au.edu.jcu.assessment.airwise.ui.learn.LearnScreen
import au.edu.jcu.assessment.airwise.ui.learn.LearnUiState
import au.edu.jcu.assessment.airwise.ui.settings.SettingsScreen
import au.edu.jcu.assessment.airwise.ui.settings.SettingsUiState
import au.edu.jcu.assessment.airwise.ui.theme.AirWiseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** GUI tests for the Home, Learn and Settings screens. */
@RunWith(AndroidJUnit4::class)
class ScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun home_aqiBadgeHasAccessibleDescription() {
        val reading = AirReading(Location.Default, "2:00 PM", 42, 6.3, 14.8, 61.0, 3.2)
        composeTestRule.setContent {
            AirWiseTheme {
                HomeScreen(HomeUiState(live = LiveAirState.Success(reading)), {}, {}, {})
            }
        }
        // TalkBack reads the number and its category together, not the colour.
        composeTestRule.onNodeWithContentDescription("US AQI 42, Good").assertIsDisplayed()
    }

    @Test
    fun home_offlineShowsRetry() {
        var retried = false
        composeTestRule.setContent {
            AirWiseTheme {
                HomeScreen(HomeUiState(live = LiveAirState.Error("No internet connection.")), { retried = true }, {}, {})
            }
        }
        composeTestRule.onNodeWithText("Try again").performClick()
        assertTrue(retried)
    }

    @Test
    fun learn_clickingLessonPassesItsId() {
        var clicked: String? = null
        composeTestRule.setContent {
            AirWiseTheme {
                LearnScreen(LearnUiState(LessonContent.lessons, emptySet()), onLessonClick = { clicked = it })
            }
        }
        composeTestRule.onNodeWithText("What is the Air Quality Index?").performClick()
        assertEquals("aqi-basics", clicked)
    }

    @Test
    fun learn_completedLessonShowsDoneLabel() {
        composeTestRule.setContent {
            AirWiseTheme {
                LearnScreen(LearnUiState(LessonContent.lessons, setOf("aqi-basics")), onLessonClick = {})
            }
        }
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }

    @Test
    fun settings_deleteAsksForConfirmationFirst() {
        var deleted = false
        composeTestRule.setContent {
            AirWiseTheme {
                SettingsScreen(SettingsUiState(), {}, {}, onDeleteAllData = { deleted = true })
            }
        }
        composeTestRule.onNodeWithText("Delete all my learning data").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Delete all learning data?").assertIsDisplayed()
        assertTrue(!deleted)
        composeTestRule.onNodeWithText("Delete").performClick()
        assertTrue(deleted)
    }
}
