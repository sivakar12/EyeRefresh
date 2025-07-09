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
import com.sivakar.eyerefresh.ui.components.EyeRefreshHeader
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.BorderStroke

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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with app title and screen title
        EyeRefreshHeader(
            title = "Session History",
            onBackClick = onBackClick
        )
        Spacer(modifier = Modifier.height(16.dp))
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
            // Show sessions list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    SessionCard(session = session)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session ${session.sessionId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = formatDuration(session.durationMs),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Started: ${formatTimestamp(session.startTime)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Completed: ${formatTimestamp(session.completeTime)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    
    return if (minutes > 0) {
        "${minutes}m ${remainingSeconds}s"
    } else {
        "${remainingSeconds}s"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
} 