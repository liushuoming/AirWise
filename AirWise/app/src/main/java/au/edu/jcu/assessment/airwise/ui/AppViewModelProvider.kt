package au.edu.jcu.assessment.airwise.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import au.edu.jcu.assessment.airwise.AirWiseApplication
import au.edu.jcu.assessment.airwise.di.AppContainer
import au.edu.jcu.assessment.airwise.domain.QuizGenerator
import au.edu.jcu.assessment.airwise.ui.home.HomeViewModel
import au.edu.jcu.assessment.airwise.ui.learn.LearnViewModel
import au.edu.jcu.assessment.airwise.ui.learn.LessonViewModel
import au.edu.jcu.assessment.airwise.ui.quiz.QuizViewModel
import au.edu.jcu.assessment.airwise.ui.settings.ProgressViewModel
import au.edu.jcu.assessment.airwise.ui.settings.SettingsViewModel

/** One factory for every ViewModel, injecting repositories from the AppContainer. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val c = container()
            HomeViewModel(c.airQualityRepository, c.settingsRepository, c.progressRepository)
        }
        initializer {
            val c = container()
            LearnViewModel(c.lessonRepository, c.progressRepository)
        }
        initializer {
            val c = container()
            LessonViewModel(
                createSavedStateHandle(),
                c.lessonRepository,
                c.progressRepository,
                c.airQualityRepository,
                c.settingsRepository
            )
        }
        initializer {
            val c = container()
            QuizViewModel(
                createSavedStateHandle(),
                c.lessonRepository,
                c.progressRepository,
                c.airQualityRepository,
                c.settingsRepository,
                QuizGenerator()
            )
        }
        initializer {
            ProgressViewModel(container().progressRepository)
        }
        initializer {
            val c = container()
            SettingsViewModel(c.settingsRepository, c.airQualityRepository, c.progressRepository)
        }
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as AirWiseApplication).container
