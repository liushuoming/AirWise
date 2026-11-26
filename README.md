# AirWise – Learn About the Air You Breathe

CP3406 Assessment 3: Education App

AirWise is an educational Android app that teaches people how air pollution affects their health. Short lessons explain the Air Quality Index (AQI), particle pollution, gas pollutants and how to protect yourself. Quizzes check what you've learned, and your progress is saved on your device.

The app's main idea is to **connect each lesson to real, live data**. A lesson on PM2.5 shows today's PM2.5 level in your chosen city, and the daily quiz opens with a question built from the current AQI ("Right now the US AQI in Townsville is 42. Which category is this?").

---

## Core features

| Feature | Screen | Implementation |
|---|---|---|
| Today's live AQI with health advice | Home | `HomeViewModel` → `AirQualityRepository` → Retrofit (Open-Meteo) |
| Learning stats at a glance (lessons, accuracy, streak) | Home | Room `Flow`s combined in `OfflineProgressRepository` |
| Six lessons in four topics, marked complete | Learn, Lesson | `LessonContent`, `LessonViewModel` with a `SavedStateHandle` argument |
| Live real-world example inside lessons | Lesson | `AirReading.valueOf(pollutant)` |
| Quizzes by topic or mixed, with explanations | Quiz | `QuizViewModel`, `QuizGenerator` |
| A quiz question generated from live data | Quiz | `QuizGenerator.liveCategoryQuestion()` |
| Persistent statistics: accuracy by topic, best score, streak, history | Progress | Room + `StatsCalculator` |
| Choose a city (presets or search) | Settings | Geocoding API, saved in Room |
| Delete all learning data | Settings | `ProgressRepository.resetProgress()`, `SettingsRepository.clear()` |
| Works offline | All | Lessons and quizzes are bundled; network errors show a retry state |

---

## Architecture

```
UI (Compose screens) ──► ViewModels (StateFlow) ──► Repositories ──► Room DAOs / Retrofit services
        ▲                         ▲
 Navigation Compose      AppViewModelProvider.Factory ◄── AppContainer (manual DI) ◄── AirWiseApplication
```

- **MVVM and unidirectional data flow.** Each screen is a stateless composable that receives a UI state and event callbacks. ViewModels expose `StateFlow`, collected with `collectAsStateWithLifecycle()` so collection stops when the screen isn't visible.
- **Repository pattern.** ViewModels depend on interfaces (`AirQualityRepository`, `ProgressRepository`, `SettingsRepository`, `LessonRepository`), not on Room or Retrofit, so tests can swap in fakes.
- **Dependency injection.** `DefaultAppContainer` builds the database, Retrofit services and repositories once. `AppViewModelProvider.Factory` injects them into every ViewModel, including a `SavedStateHandle` for navigation arguments.
- **Navigation Component.** `AirWiseNavHost` defines six destinations. `lesson/{lessonId}` and `quiz/{topic}` take arguments. The bottom bar uses `popUpTo` + `saveState`/`restoreState` so each tab keeps its state.
- **Room.** Three tables: `quiz_attempts`, `lesson_completions` and `user_settings`. DAOs return `Flow`, so statistics update automatically.
- **Networking.** Retrofit with `suspend` functions and the kotlinx.serialization converter. `loadLive()` turns `IOException`, `HttpException` and `SerializationException` into an error state instead of crashing.
- **Domain logic** (`QuizGenerator`, `StatsCalculator`) is plain Kotlin, so it can be unit tested without Android.

### Project structure

```
app/src/main/java/au/edu/jcu/assessment/airwise/
├── AirWiseApplication.kt, MainActivity.kt
├── di/AppContainer.kt                 # Manual dependency injection
├── data/
│   ├── Repositories.kt                # Repository interfaces and implementations
│   ├── LessonContent.kt               # Lessons and question bank
│   ├── local/                         # Room: entities, DAOs, database
│   └── remote/OpenMeteoApi.kt         # Retrofit services and DTOs
├── domain/                            # QuizGenerator, StatsCalculator
├── model/                             # Location, AirReading, Lesson, QuizQuestion, AqiCategory…
└── ui/
    ├── AirWiseApp.kt                  # Scaffold, top bar, bottom bar, NavHost
    ├── AppViewModelProvider.kt
    ├── navigation/Destinations.kt
    ├── components/, home/, learn/, quiz/, settings/, theme/
```

---

## Ethical design decisions

These decisions apply my Assessment 2 research and the ACS Code of Professional Ethics:

- **No location permission (ACS 2.2(d)).** The learner chooses a city instead. Only that city's coordinates are sent to Open-Meteo.
- **Data stays on the device.** Room storage only, `allowBackup="false"`, no account, ads or analytics SDKs, and a delete-all button with a confirmation dialog.
- **Honest data (ACS 2.1(b)).** Live values are labelled as modelled estimates, with their update time and the Open-Meteo source (CC BY 4.0).
- **Accessibility (ACS 2.3.1(c)(d)).** Every AQI colour has a text label. Quiz answers say "correct answer" / "your answer" in text rather than relying on colour. The AQI badge has one clear TalkBack description.
- **Health messaging (ACS 2.3.1(f)).** Wording follows US EPA categories. The app states it gives general information, not medical advice.

---

## Testing

| Type | File | What it checks |
|---|---|---|
| Unit | `QuizGeneratorTest` | Topic filtering, live question correctness, no duplicates, offline quizzes |
| Unit | `StatsCalculatorTest` | Streaks, rounding, accuracy by topic, combined stats |
| Unit | `AqiCategoryTest` | EPA category boundaries |
| Unit (ViewModel) | `QuizViewModelTest` | Navigation argument parsing, scoring, single answer per question, offline behaviour, results saved |
| Instrumented (GUI) | `QuizScreenTest` | Question display, correct/incorrect feedback in text, results screen, live label |
| Instrumented (GUI) | `ScreensTest` | Accessible AQI description, retry, lesson click, Done label, delete confirmation |
| Instrumented (Room) | `ProgressDaoTest` | Ordering, replace on conflict, delete all, settings row |

Run the unit tests with `./gradlew test` and the instrumented tests with `./gradlew connectedAndroidTest` (an emulator must be running).

---

## APIs and sources

- [Open-Meteo Air Quality API](https://open-meteo.com/en/docs/air-quality-api) and [Geocoding API](https://open-meteo.com/en/docs/geocoding-api): free, no API key, CC BY 4.0.
- Lesson content is based on [US EPA AirNow – AQI Basics](https://www.airnow.gov/aqi/aqi-basics/).

## How to run

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or device (API 26+) with internet access.
