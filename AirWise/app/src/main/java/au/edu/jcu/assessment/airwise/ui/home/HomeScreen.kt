package au.edu.jcu.assessment.airwise.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.airwise.model.AirReading
import au.edu.jcu.assessment.airwise.model.AqiCategory
import au.edu.jcu.assessment.airwise.model.LearningStats
import au.edu.jcu.assessment.airwise.model.Location
import au.edu.jcu.assessment.airwise.ui.LiveAirState
import au.edu.jcu.assessment.airwise.ui.components.AqiBadge
import au.edu.jcu.assessment.airwise.ui.components.SectionTitle
import au.edu.jcu.assessment.airwise.ui.components.StatCard
import au.edu.jcu.assessment.airwise.ui.theme.AirWiseTheme

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onStartQuiz: () -> Unit,
    onOpenLessons: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Learn about the air you breathe", style = MaterialTheme.typography.headlineSmall)

        LiveCard(uiState.location, uiState.live, onRefresh)

        SectionTitle("Your learning")
        StatsRow(uiState.stats)

        Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
            Text("Take today’s quiz")
        }
        OutlinedButton(onClick = onOpenLessons, modifier = Modifier.fillMaxWidth()) {
            Text("Browse lessons")
        }
    }
}

/** Today's real air quality: the "lesson of the day" from live data. */
@Composable
private fun LiveCard(location: Location, live: LiveAirState, onRefresh: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Right now in ${location.displayName}")
            when (live) {
                is LiveAirState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is LiveAirState.Error -> {
                    Text(live.message, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onRefresh) { Text("Try again") }
                }
                is LiveAirState.Success -> {
                    val aqi = live.reading.usAqi
                    AqiBadge(aqi)
                    if (aqi != null) {
                        Text(AqiCategory.from(aqi).advice, style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Updated ${live.reading.updatedAt} · Open-Meteo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onRefresh) { Text("Refresh") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsRow(stats: LearningStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard("Lessons done", "${stats.lessonsCompleted}/${stats.totalLessons}", Modifier.weight(1f))
        StatCard("Quiz accuracy", "${stats.accuracyPercent}%", Modifier.weight(1f))
        StatCard("Day streak", "${stats.currentStreakDays}", Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AirWiseTheme {
        HomeScreen(
            uiState = HomeUiState(
                live = LiveAirState.Success(
                    AirReading(Location.Default, "2:00 PM, 24 Nov", 42, 6.3, 14.8, 61.0, 3.2)
                ),
                stats = LearningStats(lessonsCompleted = 3, totalLessons = 6, accuracyPercent = 80, currentStreakDays = 2)
            ),
            onRefresh = {},
            onStartQuiz = {},
            onOpenLessons = {}
        )
    }
}
