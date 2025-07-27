package com.sivakar.eyerefresh.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sivakar.eyerefresh.history.CompletedSession
import com.sivakar.eyerefresh.history.Range
import com.sivakar.eyerefresh.ui.theme.EyeRefreshTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EyeRefreshTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HistoryScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(onBackClick: () -> Unit) {
    val viewModel: HistoryViewModel = viewModel()
    val sessions by viewModel.completedSessions.collectAsState(initial = emptyList())
    val selectedRange by viewModel.selectedRange.collectAsState()
    
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title at the top center
                Text(
                    text = "Session History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
                
                // Back button at the top left
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            RangeSelectionControls(
                selectedRange = selectedRange,
                onRangeSelected = { viewModel.selectRange(it) },
                onPreviousRange = { viewModel.navigateToPreviousRange() },
                onNextRange = { viewModel.navigateToNextRange() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (sessions.isEmpty()) {
                // Show empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No completed sessions in selected range",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Show chart and sessions list
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Bar chart
                    SessionBarChart(
                        sessions = sessions,
                        selectedRange = selectedRange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sessions list
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions) { session ->
                            SessionCard(session = session)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RangeSelectionControls(
    selectedRange: Range?,
    onRangeSelected: (Range) -> Unit,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Time Range",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val rangeTypes = listOf("Day", "Week", "Month", "Year")
        val currentRangeType = when (selectedRange) {
            is Range.Day -> "Day"
            is Range.Week -> "Week"
            is Range.Month -> "Month"
            is Range.Year -> "Year"
            else -> "Day" // Default to Day if no range is selected
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rangeTypes.forEach { rangeType ->
                val isSelected = rangeType == currentRangeType
                Button(
                    onClick = {
                        val newRange = when (rangeType) {
                            "Day" -> {
                                val now = LocalDateTime.now()
                                Range.Day(now.dayOfMonth, now.monthValue, now.year)
                            }
                            "Week" -> {
                                val now = LocalDateTime.now()
                                val weekOfYear = now.get(java.time.temporal.WeekFields.ISO.weekOfYear())
                                val weekOfMonth = ((weekOfYear - LocalDateTime.of(now.year, now.month, 1, 0, 0)
                                    .get(java.time.temporal.WeekFields.ISO.weekOfYear())) + 1).toInt()
                                Range.Week(weekOfMonth, now.monthValue, now.year)
                            }
                            "Month" -> {
                                val now = LocalDateTime.now()
                                Range.Month(now.monthValue, now.year)
                            }
                            "Year" -> {
                                val now = LocalDateTime.now()
                                Range.Year(now.year)
                            }
                            else -> {
                                val now = LocalDateTime.now()
                                Range.Day(now.dayOfMonth, now.monthValue, now.year)
                            }
                        }
                        onRangeSelected(newRange)
                    },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 1.dp, minHeight = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = null,
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFF4CAF50) /* Green */ else Color.LightGray
                    )
                ) {
                    Text(
                        text = rangeType,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousRange) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous"
                )
            }
            Text(
                text = formatRangeDisplay(selectedRange ?: getCurrentDayRange()),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            IconButton(onClick = onNextRange) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next"
                )
            }
        }
    }
}

private fun getCurrentDayRange(): Range.Day {
    val now = LocalDateTime.now()
    return Range.Day(now.dayOfMonth, now.monthValue, now.year)
}

private fun formatRangeDisplay(range: Range): String {
    return when (range) {
        is Range.Day -> {
            val date = LocalDate.of(range.year, range.month, range.day)
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            date.format(formatter)
        }
        is Range.Week -> {
            val firstDayOfMonth = LocalDate.of(range.year, range.month, 1)
            val firstDayOfFirstWeek = firstDayOfMonth.minusDays(
                firstDayOfMonth.dayOfWeek.value.toLong() - 1
            )
            val startOfTargetWeek = firstDayOfFirstWeek.plusDays((range.weekNumber - 1) * 7L)
            val endOfTargetWeek = startOfTargetWeek.plusDays(6)
            val formatter = DateTimeFormatter.ofPattern("MMM d")
            val startStr = startOfTargetWeek.format(formatter)
            val endStr = endOfTargetWeek.format(formatter)
            val yearStr = endOfTargetWeek.year
            "$startStr - $endStr, $yearStr"
        }
        is Range.Month -> {
            val date = LocalDate.of(range.year, range.month, 1)
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
            date.format(formatter)
        }
        is Range.Year -> {
            range.year.toString()
        }
        else -> {
            "Today"
        }
    }
}

@Composable
fun SessionCard(session: CompletedSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatTimestamp(session.startTime),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}



private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
} 

@Composable
fun SessionBarChart(
    sessions: List<CompletedSession>,
    selectedRange: Range?,
    modifier: Modifier = Modifier
) {
    if (selectedRange == null || sessions.isEmpty()) {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data to display",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    val subsections = Range.getSubsections(selectedRange)
    val sessionCounts = subsections.map { subsection ->
        val (startTime, endTime) = Range.getStartTimestampAndEndTimestamp(subsection)
        val count = sessions.count { session ->
            session.startTime >= startTime && session.startTime <= endTime
        }
        Pair(subsection, count)
    }

    val maxCount = sessionCounts.maxOfOrNull { it.second } ?: 0

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Session Count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (maxCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sessions in this period",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Chart area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        val barWidth = size.width / sessionCounts.size
                        val maxBarHeight = size.height - 20.dp.toPx() // Leave space for labels
                        
                        // Draw subtle grid line
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, size.height - 20.dp.toPx()),
                            end = Offset(size.width, size.height - 20.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )
                        
                        sessionCounts.forEachIndexed { index, (subsection, count) ->
                            val barHeight = if (maxCount > 0) {
                                (count.toFloat() / maxCount.toFloat()) * maxBarHeight
                            } else 0f
                            
                            val x = index * barWidth + barWidth / 2
                            val y = size.height - 20.dp.toPx() - barHeight
                            
                            // Draw bar with rounded corners
                            if (barHeight > 0) {
                                drawRoundRect(
                                    color = primaryColor,
                                    topLeft = Offset(x - barWidth * 0.4f, y),
                                    size = Size(barWidth * 0.8f, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                )
                            }
                        }
                    }
                }
                
                // X-axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    sessionCounts.forEach { (subsection, _) ->
                        Text(
                            text = formatSubsectionLabel(subsection, selectedRange),
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatSubsectionLabel(subsection: Range, selectedRange: Range?): String {
    return when (subsection) {
        is Range.Hour -> {
            (subsection.hour + 1).toString()
        }
        is Range.Day -> {
            when (selectedRange) {
                is Range.Week -> {
                    // For week view, show "July 1" format
                    val date = LocalDate.of(subsection.year, subsection.month, subsection.day)
                    val formatter = DateTimeFormatter.ofPattern("MMMM d")
                    date.format(formatter)
                }
                is Range.Month -> {
                    // For month view, show just the day number
                    subsection.day.toString()
                }
                else -> subsection.day.toString()
            }
        }
        is Range.Month -> {
            val date = LocalDate.of(subsection.year, subsection.month, 1)
            val formatter = DateTimeFormatter.ofPattern("MMM")
            date.format(formatter)
        }
        else -> ""
    }
} 