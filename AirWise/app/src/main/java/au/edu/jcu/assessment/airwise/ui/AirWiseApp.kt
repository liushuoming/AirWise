package au.edu.jcu.assessment.airwise.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import au.edu.jcu.assessment.airwise.ui.home.HomeScreen
import au.edu.jcu.assessment.airwise.ui.home.HomeViewModel
import au.edu.jcu.assessment.airwise.ui.learn.LearnScreen
import au.edu.jcu.assessment.airwise.ui.learn.LearnViewModel
import au.edu.jcu.assessment.airwise.ui.learn.LessonScreen
import au.edu.jcu.assessment.airwise.ui.learn.LessonViewModel
import au.edu.jcu.assessment.airwise.ui.navigation.HomeDestination
import au.edu.jcu.assessment.airwise.ui.navigation.LearnDestination
import au.edu.jcu.assessment.airwise.ui.navigation.LessonDestination
import au.edu.jcu.assessment.airwise.ui.navigation.ProgressDestination
import au.edu.jcu.assessment.airwise.ui.navigation.QuizDestination
import au.edu.jcu.assessment.airwise.ui.navigation.SettingsDestination
import au.edu.jcu.assessment.airwise.ui.navigation.TopLevelDestination
import au.edu.jcu.assessment.airwise.ui.navigation.topLevelDestinations
import au.edu.jcu.assessment.airwise.ui.quiz.QuizScreen
import au.edu.jcu.assessment.airwise.ui.quiz.QuizViewModel
import au.edu.jcu.assessment.airwise.ui.settings.ProgressScreen
import au.edu.jcu.assessment.airwise.ui.settings.ProgressViewModel
import au.edu.jcu.assessment.airwise.ui.settings.SettingsScreen
import au.edu.jcu.assessment.airwise.ui.settings.SettingsViewModel

/** App shell: top bar, bottom navigation and the navigation graph. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirWiseApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isTopLevel = topLevelDestinations.any { currentDestination.isOn(it.route) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleFor(currentDestination)) },
                navigationIcon = {
                    if (!isTopLevel && navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination.isOn(destination.route),
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AirWiseNavHost(navController, Modifier.padding(innerPadding))
    }
}

@Composable
fun AirWiseNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(HomeDestination.route) {
            val viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                uiState = state,
                onRefresh = viewModel::refresh,
                onStartQuiz = { navController.navigate(QuizDestination.createRoute()) },
                onOpenLessons = { navController.navigateToTopLevel(LearnDestination) }
            )
        }
        composable(LearnDestination.route) {
            val viewModel: LearnViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LearnScreen(
                uiState = state,
                onLessonClick = { navController.navigate(LessonDestination.createRoute(it)) }
            )
        }
        composable(
            route = LessonDestination.routeWithArgs,
            arguments = listOf(navArgument(LessonDestination.LESSON_ID_ARG) { type = NavType.StringType })
        ) {
            val viewModel: LessonViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LessonScreen(
                uiState = state,
                onMarkComplete = viewModel::markComplete,
                onRetryLive = viewModel::loadLiveExample,
                onStartQuiz = { topic -> navController.navigate(QuizDestination.createRoute(topic.name)) }
            )
        }
        composable(
            route = QuizDestination.routeWithArgs,
            arguments = listOf(navArgument(QuizDestination.TOPIC_ARG) { type = NavType.StringType })
        ) {
            val viewModel: QuizViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            QuizScreen(
                uiState = state,
                onSelectAnswer = viewModel::selectAnswer,
                onNext = viewModel::nextQuestion,
                onRestart = viewModel::startQuiz,
                onFinish = { navController.navigateUp() }
            )
        }
        composable(ProgressDestination.route) {
            val viewModel: ProgressViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val stats by viewModel.uiState.collectAsStateWithLifecycle()
            ProgressScreen(
                stats = stats,
                onStartQuiz = { navController.navigate(QuizDestination.createRoute()) }
            )
        }
        composable(SettingsDestination.route) {
            val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                uiState = state,
                onSelectLocation = viewModel::selectLocation,
                onSearch = viewModel::searchCity,
                onDeleteAllData = viewModel::deleteAllData
            )
        }
    }
}

/** Standard bottom-navigation behaviour: one copy of each tab, state saved. */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.isOn(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true

private fun titleFor(destination: NavDestination?): String {
    val route = destination?.route ?: return "AirWise"
    return when {
        route == HomeDestination.route -> "AirWise"
        route.startsWith(LessonDestination.route) -> LessonDestination.title
        route.startsWith(QuizDestination.route) -> QuizDestination.title
        else -> topLevelDestinations.firstOrNull { it.route == route }?.title ?: "AirWise"
    }
}
