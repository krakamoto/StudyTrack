package com.studytrack.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studytrack.domain.usecases.ChartPoint
import com.studytrack.domain.usecases.HourlyPoint
import com.studytrack.domain.usecases.ProductivityAnalytics
import com.studytrack.domain.usecases.WeekdayPoint
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val PositiveGreen = Color(0xFF4CAF50)
private val NegativeRed = Color(0xFFE57373)

private fun millisToHours(millis: Long): Float = millis / 3_600_000f

private fun formatDuration(millis: Long): String {
    val totalMinutes = millis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val analytics = state.analytics
    val tabs = listOf("Giornaliero", "Settimanale", "Mensile")

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
                    .padding(top = 24.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Statistiche",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Produttività, costanza e andamento nel tempo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            FocusScoreCard(
                analytics = analytics,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("Oggi", formatDuration(analytics.totalTodayMillis), Modifier.weight(1f))
                MetricCard("Settimana", formatDuration(analytics.totalThisWeekMillis), Modifier.weight(1f))
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("Sessioni 7gg", analytics.sessionsLast7Days.toString(), Modifier.weight(1f))
                MetricCard("Media sessione", formatDuration(analytics.avgSessionMillis), Modifier.weight(1f))
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(label, maxLines = 1) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            val chartData = when (state.selectedTab) {
                0 -> analytics.dailyTotals
                1 -> analytics.weeklyTotals
                else -> analytics.monthlyTotals
            }
            val chartTitle = when (state.selectedTab) {
                0 -> "Ultimi 7 giorni"
                1 -> "Ultime 8 settimane"
                else -> "Ultimi 12 mesi"
            }
            BarChartSection(
                title = chartTitle,
                data = chartData,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            TrendCard(
                analytics = analytics,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            WhenYouStudyCard(
                analytics = analytics,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            InsightCard(
                analytics = analytics,
                projectName = state.projectName(analytics.topProjectId) ?: "Nessun progetto",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "PER PROGETTO · ULTIMI 30 GIORNI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            if (analytics.projectTotals.isEmpty()) {
                Text(
                    text = "Nessun dato disponibile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            } else {
                val totalMs = analytics.projectTotals.sumOf { it.totalMillis }.coerceAtLeast(1L)
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    analytics.projectTotals.forEach { project ->
                        val percentage = project.totalMillis.toFloat() / totalMs.toFloat()
                        val color = state.projectColor(project.projectId)?.let {
                            try {
                                Color(android.graphics.Color.parseColor(it))
                            } catch (_: Exception) {
                                null
                            }
                        } ?: MaterialTheme.colorScheme.primary
                        ProjectBreakdownRow(
                            name = state.projectName(project.projectId) ?: "Nessun progetto",
                            color = color,
                            percentage = percentage,
                            duration = formatDuration(project.totalMillis)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusScoreCard(
    analytics: ProductivityAnalytics,
    modifier: Modifier = Modifier
) {
    var showInfo by remember { mutableStateOf(false) }
    val animatedScore by animateFloatAsState(
        targetValue = analytics.focusScore.toFloat(),
        label = "focusScore"
    )
    val scoreLabel = when {
        analytics.focusScore >= 80 -> "Eccellente"
        analytics.focusScore >= 60 -> "Buono"
        analytics.focusScore >= 40 -> "In crescita"
        analytics.hasData -> "Da costruire"
        else -> "Nessun dato"
    }
    val scoreColor = when {
        analytics.focusScore >= 60 -> PositiveGreen
        analytics.focusScore >= 40 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRODUTTIVITÀ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                IconButton(
                    onClick = { showInfo = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Come viene calcolato il Focus Score",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(112.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 10.dp.toPx()
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = scoreColor,
                            startAngle = -90f,
                            sweepAngle = 360f * (animatedScore / 100f),
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = analytics.focusScore.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = scoreLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scoreColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TrendBadge(analytics.trendPercent)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${analytics.activeDaysLast7Days}/7 giorni attivi · " +
                            "${analytics.consistencyPercent}% costanza",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Streak attuale: ${analytics.streakDays} " +
                            if (analytics.streakDays == 1) "giorno" else "giorni",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Il punteggio combina costanza, tempo studiato, durata media " +
                    "delle sessioni e streak.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Come funziona il Focus Score") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Il Focus Score è un indicatore da 0 a 100; non è un giudizio " +
                            "sul rendimento, ma un modo per rendere visibili abitudini e costanza."
                    )
                    FocusCriterion(
                        points = "40 punti",
                        title = "Costanza",
                        detail = "Giorni con almeno una sessione negli ultimi 7, " +
                            "rapportati a 7 giorni."
                    )
                    FocusCriterion(
                        points = "30 punti",
                        title = "Volume di studio",
                        detail = "Media giornaliera degli ultimi 7 giorni rispetto " +
                            "a un obiettivo di 2 ore al giorno."
                    )
                    FocusCriterion(
                        points = "20 punti",
                        title = "Qualità della sessione",
                        detail = "Durata media delle sessioni degli ultimi 7 giorni " +
                            "rispetto a 45 minuti."
                    )
                    FocusCriterion(
                        points = "10 punti",
                        title = "Streak",
                        detail = "Giorni consecutivi di studio rispetto a una settimana."
                    )
                    Text(
                        text = "I quattro contributi vengono sommati e arrotondati al numero " +
                            "intero più vicino.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Ho capito") }
            }
        )
    }
}

@Composable
private fun FocusCriterion(points: String, title: String, detail: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        ) {
            Text(
                text = points,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrendBadge(percent: Int?) {
    val text: String
    val color: Color
    when {
        percent == null -> {
            text = "Storico insufficiente"
            color = MaterialTheme.colorScheme.onSurfaceVariant
        }
        percent > 0 -> {
            text = "▲ +$percent% vs 7 giorni prima"
            color = PositiveGreen
        }
        percent < 0 -> {
            text = "▼ $percent% vs 7 giorni prima"
            color = NegativeRed
        }
        else -> {
            text = "— invariato vs 7 giorni prima"
            color = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
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
    title: String,
    data: List<ChartPoint>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (data.isEmpty() || data.all { it.totalMillis == 0L }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun dato per questo periodo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                return@Column
            }

            val maxHours = (data.maxOfOrNull { millisToHours(it.totalMillis) } ?: 1f)
                .coerceAtLeast(1f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    val hours = millisToHours(item.totalMillis)
                    val ratio = (hours / maxHours).coerceIn(0f, 1f)
                    val animatedRatio by animateFloatAsState(
                        targetValue = ratio,
                        label = "barHeight"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (hours > 0f) {
                            Text(
                                text = "%.1fh".format(hours),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.62f)
                                .height(
                                    (120 * animatedRatio).dp.coerceAtLeast(
                                        if (hours > 0f) 6.dp else 2.dp
                                    )
                                )
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (hours > 0f) accentColor
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendCard(
    analytics: ProductivityAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ANDAMENTO 14 GIORNI",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tempo giornaliero e media mobile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TrendBadge(analytics.trendPercent)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (analytics.last14Days.all { it.totalMillis == 0L }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Registra qualche sessione per vedere il trend",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val dailyColor = MaterialTheme.colorScheme.primary
                val averageColor = MaterialTheme.colorScheme.secondary
                val gridColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val points = analytics.last14Days
                    val maxValue = points.maxOf {
                        maxOf(it.totalMillis, it.movingAverageMillis)
                    }.coerceAtLeast(1L).toFloat()
                    val xStep = if (points.size > 1) size.width / (points.size - 1) else size.width
                    val chartHeight = size.height - 8.dp.toPx()
                    fun x(index: Int) = index * xStep
                    fun y(value: Long) = chartHeight - (value / maxValue * chartHeight)

                    for (i in 1..3) {
                        val gridY = chartHeight * i / 4f
                        drawLine(
                            color = gridColor.copy(alpha = 0.55f),
                            start = Offset(0f, gridY),
                            end = Offset(size.width, gridY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val dailyPath = Path()
                    val averagePath = Path()
                    val fillPath = Path()
                    points.forEachIndexed { index, point ->
                        val px = x(index)
                        val py = y(point.totalMillis)
                        if (index == 0) {
                            dailyPath.moveTo(px, py)
                            averagePath.moveTo(px, y(point.movingAverageMillis))
                            fillPath.moveTo(px, chartHeight)
                            fillPath.lineTo(px, py)
                        } else {
                            dailyPath.lineTo(px, py)
                            averagePath.lineTo(px, y(point.movingAverageMillis))
                            fillPath.lineTo(px, py)
                        }
                    }
                    fillPath.lineTo(x(points.lastIndex), chartHeight)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                dailyColor.copy(alpha = 0.24f),
                                dailyColor.copy(alpha = 0.01f)
                            )
                        ),
                        style = Fill
                    )
                    drawPath(
                        path = dailyPath,
                        color = dailyColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = averagePath,
                        color = averageColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )
                    )
                    points.forEachIndexed { index, point ->
                        drawCircle(
                            color = dailyColor,
                            radius = 3.dp.toPx(),
                            center = Offset(x(index), y(point.totalMillis))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = analytics.last14Days.first().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = analytics.last14Days[analytics.last14Days.size / 2].label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = analytics.last14Days.last().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(color = dailyColor, text = "Tempo giornaliero")
                    LegendDot(color = averageColor, text = "Media 7 giorni")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WhenYouStudyCard(
    analytics: ProductivityAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "QUANDO STUDI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Ore e giorni più produttivi degli ultimi 30 giorni",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightMiniCard(
                    label = "Ora top",
                    value = analytics.peakHour?.let { "%02d:00–%02d:00".format(it, (it + 1) % 24) }
                        ?: "—",
                    detail = formatDuration(analytics.peakHourMillis),
                    modifier = Modifier.weight(1f)
                )
                InsightMiniCard(
                    label = "Giorno top",
                    value = analytics.peakWeekday?.let { day ->
                        day.getDisplayName(TextStyle.FULL, Locale.getDefault())
                            .replaceFirstChar { it.titlecase() }
                    } ?: "—",
                    detail = formatDuration(analytics.peakWeekdayMillis),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Distribuzione oraria",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            HourlyBars(analytics.hourlyTotals, analytics.peakHour)

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Distribuzione settimanale",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            WeekdayBars(analytics.weekdayTotals, analytics.peakWeekday)
        }
    }
}

@Composable
private fun InsightMiniCard(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HourlyBars(points: List<HourlyPoint>, peakHour: Int?) {
    val maxValue = (points.maxOfOrNull { it.totalMillis } ?: 0L).coerceAtLeast(1L)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            points.forEach { point ->
                val ratio = point.totalMillis.toFloat() / maxValue
                val isPeak = point.hour == peakHour
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(
                                if (point.totalMillis > 0L) ratio.coerceAtLeast(0.08f) else 0.03f
                            )
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(
                                when {
                                    isPeak -> MaterialTheme.colorScheme.primary
                                    point.totalMillis > 0L -> MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            points.forEachIndexed { index, _ ->
                Box(modifier = Modifier.weight(1f)) {
                    if (index % 6 == 0 || index == points.lastIndex) {
                        Text(
                            text = "%02d".format(index),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayBars(points: List<WeekdayPoint>, peakDay: DayOfWeek?) {
    val maxValue = (points.maxOfOrNull { it.totalMillis } ?: 0L).coerceAtLeast(1L)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            val ratio = point.totalMillis.toFloat() / maxValue
            val isPeak = point.day == peakDay
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .fillMaxHeight(
                                if (point.totalMillis > 0L) ratio.coerceAtLeast(0.08f) else 0.03f
                            )
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                when {
                                    isPeak -> MaterialTheme.colorScheme.secondary
                                    point.totalMillis > 0L -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.42f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    analytics: ProductivityAnalytics,
    projectName: String,
    modifier: Modifier = Modifier
) {
    val suggestion = when {
        !analytics.hasData ->
            "Inizia a registrare le tue sessioni: dopo qualche giorno vedrai qui " +
                "i tuoi pattern di produttività."
        analytics.trendPercent != null && analytics.trendPercent <= -15 ->
            "Il tempo studiato è calato rispetto alla settimana precedente. " +
                "Prova a pianificare una sessione breve ma costante."
        analytics.consistencyPercent < 50 ->
            "La costanza è il fattore più importante: punta a studiare anche solo " +
                "20 minuti in più giorni alla settimana."
        analytics.avgSessionMillis in 1..(20 * 60_000L) ->
            "Le sessioni sono piuttosto brevi. Prova blocchi da 25–45 minuti " +
                "per aumentare la concentrazione."
        analytics.focusScore >= 75 ->
            "Ottimo ritmo! Mantieni la routine attuale e proteggi le ore in cui " +
                "sei più produttivo."
        else ->
            "Continua così: costanza e sessioni regolari stanno costruendo " +
                "il tuo andamento."
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "INSIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sessione più lunga: ${formatDuration(analytics.longestSessionMillis)} · " +
                        "Progetto top: $projectName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProjectBreakdownRow(
    name: String,
    color: Color,
    percentage: Float,
    duration: String
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
                text = "${(percentage * 100).toInt()}% · $duration",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
