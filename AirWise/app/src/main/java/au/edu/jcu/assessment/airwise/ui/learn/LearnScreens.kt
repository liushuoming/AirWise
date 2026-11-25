package au.edu.jcu.assessment.airwise.ui.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.airwise.data.LessonContent
import au.edu.jcu.assessment.airwise.model.Lesson
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.model.Topic
import au.edu.jcu.assessment.airwise.ui.LiveAirState
import au.edu.jcu.assessment.airwise.ui.components.SectionTitle
import au.edu.jcu.assessment.airwise.ui.theme.AirWiseTheme
import java.util.Locale

// ---------------- Lesson list ----------------

@Composable
fun LearnScreen(
    uiState: LearnUiState,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        Topic.entries.forEach { topic ->
            val lessons = uiState.lessons.filter { it.topic == topic }
            if (lessons.isNotEmpty()) {
                item(key = "header-${topic.name}") {
                    SectionTitle(topic.title, Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp))
                }
                items(lessons, key = { it.id }) { lesson ->
                    LessonRow(lesson, lesson.id in uiState.completedIds) { onLessonClick(lesson.id) }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, isCompleted: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(lesson.title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("${lesson.summary} · ${lesson.minutes} min") },
        trailingContent = {
            if (isCompleted) {
                // Icon plus text, so completion isn't shown by an icon alone.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Done", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick)
    )
}

// ---------------- Single lesson ----------------

@Composable
fun LessonScreen(
    uiState: LessonUiState,
    onMarkComplete: () -> Unit,
    onRetryLive: () -> Unit,
    onStartQuiz: (Topic) -> Unit,
    modifier: Modifier = Modifier
) {
    val lesson = uiState.lesson
    if (lesson == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("This lesson could not be found.")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(lesson.topic.title.uppercase(Locale.getDefault()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(lesson.title, style = MaterialTheme.typography.headlineSmall)

        lesson.sections.forEach { section ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(section.heading, style = MaterialTheme.typography.titleMedium)
                Text(section.body, style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (lesson.livePollutant != null && uiState.live != null) {
            LiveExampleCard(lesson, uiState.location, uiState.live, onRetryLive)
        }

        if (uiState.isCompleted) {
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                Text("Lesson completed")
            }
        } else {
            Button(onClick = onMarkComplete, modifier = Modifier.fillMaxWidth()) {
                Text("Mark as complete")
            }
        }
        OutlinedButton(onClick = { onStartQuiz(lesson.topic) }, modifier = Modifier.fillMaxWidth()) {
            Text("Quiz: ${lesson.topic.title}")
        }
    }
}

/** Connects the lesson to the learner's city using today's real reading. */
@Composable
private fun LiveExampleCard(lesson: Lesson, location: Location, live: LiveAirState, onRetry: () -> Unit) {
    val pollutant = lesson.livePollutant ?: return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Real-world example")
            when (live) {
                is LiveAirState.Loading -> CircularProgressIndicator()
                is LiveAirState.Error -> {
                    Text(live.message)
                    TextButton(onClick = onRetry) { Text("Try again") }
                }
                is LiveAirState.Success -> {
                    val value = live.reading.valueOf(pollutant)
                    val text = if (value == null) {
                        "No ${pollutant.label} reading is available for ${location.name} right now."
                    } else {
                        String.format(Locale.getDefault(), "Right now %s in %s is %.1f µg/m³.", pollutant.label, location.name, value)
                    }
                    Text(text, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Modelled estimate from Open-Meteo, updated ${live.reading.updatedAt}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LearnScreenPreview() {
    AirWiseTheme {
        LearnScreen(LearnUiState(LessonContent.lessons, setOf("aqi-basics")), onLessonClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonScreenPreview() {
    AirWiseTheme {
        LessonScreen(
            uiState = LessonUiState(lesson = LessonContent.lessons[1], live = LiveAirState.Loading),
            onMarkComplete = {},
            onRetryLive = {},
            onStartQuiz = {}
        )
    }
}
