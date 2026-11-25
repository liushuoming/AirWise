package au.edu.jcu.assessment.airwise.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.airwise.model.AttemptSummary
import au.edu.jcu.assessment.airwise.model.LearningStats
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.model.Topic
import au.edu.jcu.assessment.airwise.ui.components.SectionTitle
import au.edu.jcu.assessment.airwise.ui.components.StatCard
import au.edu.jcu.assessment.airwise.ui.theme.AirWiseTheme
import java.text.DateFormat
import java.util.Date

// ---------------- Progress ----------------

@Composable
fun ProgressScreen(
    stats: LearningStats,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Lessons done", "${stats.lessonsCompleted}/${stats.totalLessons}", Modifier.weight(1f))
            StatCard("Quizzes taken", "${stats.quizzesTaken}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Overall accuracy", "${stats.accuracyPercent}%", Modifier.weight(1f))
            StatCard("Best score", "${stats.bestScorePercent}%", Modifier.weight(1f))
            StatCard("Day streak", "${stats.currentStreakDays}", Modifier.weight(1f))
        }

        SectionTitle("Accuracy by topic")
        if (stats.topicAccuracy.isEmpty()) {
            Text("Take a topic quiz from any lesson to see your accuracy here.")
        } else {
            Topic.entries.forEach { topic ->
                stats.topicAccuracy[topic]?.let { percent -> TopicAccuracyRow(topic, percent) }
            }
        }

        SectionTitle("Recent quizzes")
        if (stats.recentAttempts.isEmpty()) {
            Text("No quizzes yet.")
            Button(onClick = onStartQuiz) { Text("Take your first quiz") }
        } else {
            stats.recentAttempts.forEach { RecentAttemptRow(it) }
        }
    }
}

@Composable
private fun TopicAccuracyRow(topic: Topic, percent: Int) {
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(topic.title)
            Text("$percent%")
        }
        LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun RecentAttemptRow(attempt: AttemptSummary) {
    val date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(attempt.timestamp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(attempt.topic?.title ?: "Mixed quiz", style = MaterialTheme.typography.bodyLarge)
            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("${attempt.correct}/${attempt.total}", style = MaterialTheme.typography.titleMedium)
    }
}

// ---------------- Settings ----------------

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSelectLocation: (Location) -> Unit,
    onSearch: (String) -> Unit,
    onDeleteAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("City for live examples")
        Text("Current: ${uiState.location.displayName}")
        Location.Presets.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { location ->
                    val selected = location.name == uiState.location.name
                    if (selected) {
                        Button(onClick = {}, modifier = Modifier.weight(1f)) { Text(location.name) }
                    } else {
                        OutlinedButton(onClick = { onSelectLocation(location) }, modifier = Modifier.weight(1f)) {
                            Text(location.name)
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search a city") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                modifier = Modifier.weight(1f)
            )
            if (uiState.isSearching) {
                CircularProgressIndicator(Modifier.size(32.dp))
            } else {
                Button(onClick = { onSearch(query) }, enabled = query.isNotBlank()) { Text("Go") }
            }
        }
        uiState.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

        HorizontalDivider()
        SectionTitle("Your privacy")
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• AirWise never asks for your location. You choose a city instead.")
                Text("• Your lessons, quiz results and settings are stored only on this device, with no account, ads or analytics.")
                Text("• Only the chosen city’s coordinates are sent to Open-Meteo to get live data.")
                Text("• Health information is general guidance, not medical advice.")
            }
        }
        OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Delete all my learning data")
        }

        HorizontalDivider()
        SectionTitle("Data source")
        Text(
            "Air quality and geocoding data: Open-Meteo.com, licensed under CC BY 4.0. Live values are modelled estimates, not official measurements.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete all learning data?") },
            text = { Text("This removes your quiz history, completed lessons and chosen city from this device. It can’t be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAllData()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressScreenPreview() {
    AirWiseTheme {
        ProgressScreen(
            stats = LearningStats(
                lessonsCompleted = 4, totalLessons = 6, quizzesTaken = 3, accuracyPercent = 73,
                bestScorePercent = 100, currentStreakDays = 2,
                topicAccuracy = mapOf(Topic.AQI_BASICS to 80, Topic.PARTICLES to 60),
                recentAttempts = listOf(AttemptSummary(null, 4, 5, 0L))
            ),
            onStartQuiz = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AirWiseTheme {
        SettingsScreen(SettingsUiState(), onSelectLocation = {}, onSearch = {}, onDeleteAllData = {})
    }
}
