package au.edu.jcu.assessment.airwise.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** A screen the app can navigate to. */
interface NavigationDestination {
    val route: String
    val title: String
}

/** A screen shown in the bottom navigation bar. */
interface TopLevelDestination : NavigationDestination {
    val icon: ImageVector
}

object HomeDestination : TopLevelDestination {
    override val route = "home"
    override val title = "Home"
    override val icon = Icons.Default.Home
}

object LearnDestination : TopLevelDestination {
    override val route = "learn"
    override val title = "Learn"
    override val icon = Icons.Default.Info
}

object ProgressDestination : TopLevelDestination {
    override val route = "progress"
    override val title = "Progress"
    override val icon = Icons.Default.DateRange
}

object SettingsDestination : TopLevelDestination {
    override val route = "settings"
    override val title = "Settings"
    override val icon = Icons.Default.Settings
}

object LessonDestination : NavigationDestination {
    override val route = "lesson"
    override val title = "Lesson"
    const val LESSON_ID_ARG = "lessonId"
    val routeWithArgs = "$route/{$LESSON_ID_ARG}"
    fun createRoute(lessonId: String) = "$route/$lessonId"
}

object QuizDestination : NavigationDestination {
    override val route = "quiz"
    override val title = "Quiz"
    const val TOPIC_ARG = "topic"
    /** Passed instead of a topic name for a mixed quiz. */
    const val MIXED = "MIXED"
    val routeWithArgs = "$route/{$TOPIC_ARG}"
    fun createRoute(topicName: String = MIXED) = "$route/$topicName"
}

val topLevelDestinations = listOf(HomeDestination, LearnDestination, ProgressDestination, SettingsDestination)
