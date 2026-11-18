package com.studytrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.studytrack.data.dao.DailyTotal

private fun millisToHours(millis: Long): Float = millis / 3_600_000f

private fun formatHours(millis: Long): String {
    val h = millis / 3_600_000
    val m = (millis % 3_600_000) / 60_000
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Daily", "Weekly", "Monthly")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { i, label ->
                    Tab(
                        selected = state.selectedTab == i,
                        onClick = { viewModel.selectTab(i) },
                        text = { Text(label) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (state.selectedTab) {
                    0 -> {
                        StatCard("Today", formatHours(state.totalTodayMillis), Modifier.weight(1f))
                        StatCard("Best Day", formatHours(state.bestDayMillis), Modifier.weight(1f))
                        StatCard("Avg/Day", formatHours(state.avgDailyMillis), Modifier.weight(1f))
                    }
                    1 -> {
                        StatCard("This Week", formatHours(state.totalThisWeekMillis), Modifier.weight(1f))
                        StatCard("Best Day", formatHours(state.bestDayMillis), Modifier.weight(1f))
                        StatCard("Streak", "${state.streakDays}d 🔥", Modifier.weight(1f))
                    }
                    2 -> {
                        StatCard("Streak", "${state.streakDays}d 🔥", Modifier.weight(1f))
                        StatCard("Avg/Day", formatHours(state.avgDailyMillis), Modifier.weight(1f))
                        StatCard("Best Day", formatHours(state.bestDayMillis), Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            val chartData = when (state.selectedTab) {
                0 -> state.dailyTotals
                1 -> state.weeklyTotals
                else -> state.monthlyTotals
            }
            BarChartSection(
                data = chartData,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BY PROJECT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            if (state.projectTotals.isEmpty()) {
                Text(
                    text = "No data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            } else {
                val totalMs = state.projectTotals.sumOf { it.totalMillis }.coerceAtLeast(1L)
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.projectTotals.forEach { pt ->
                        val pct = pt.totalMillis.toFloat() / totalMs.toFloat()
                        val pColor = state.projectColor(pt.projectId)?.let {
                            try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
                        } ?: MaterialTheme.colorScheme.primary
                        ProjectBreakdownRow(
                            name = state.projectName(pt.projectId) ?: "No Project",
                            color = pColor,
                            pct = pct,
                            durationStr = formatHours(pt.totalMillis)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BarChartSection(
    data: List<DailyTotal>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data for this period",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(data.map { millisToHours(it.totalMillis) })
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { _, value, _ ->
                        data.getOrNull(value.toInt())?.date?.takeLast(5) ?: ""
                    }
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        )
    }
}

@Composable
private fun ProjectBreakdownRow(
    name: String,
    color: Color,
    pct: Float,
    durationStr: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(pct * 100).toInt()}% · $durationStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
