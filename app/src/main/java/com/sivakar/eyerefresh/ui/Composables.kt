package com.sivakar.eyerefresh.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.sivakar.eyerefresh.core.AppEvent
import com.sivakar.eyerefresh.core.AppState
import com.sivakar.eyerefresh.core.Config
import com.sivakar.eyerefresh.ui.theme.EyeRefreshTheme
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    appState: AppState,
    onEvent: (AppEvent) -> Unit,
    handleNavigation: (location: String) -> Unit
    ) {
    EyeRefreshTheme {
        Scaffold(
            topBar = {
                AppHeader(
                    onSettingsClick = {
                        handleNavigation("settings")
                    },
                    onHistoryClick = {
                        handleNavigation("history")
                    }
                )
            },
            content = {
                when (appState) {
                    is AppState.Paused -> PausedStateScreen(onEvent)
                    is AppState.TimeLeftForNextRefresh -> ScheduledStateScreen(appState, onEvent)
                    is AppState.RefreshCanStart -> RefreshCanStartStateScreen(onEvent)
                    is AppState.RefreshHappening -> RefreshStateScreen(appState, onEvent)
                    is AppState.WaitingForRefreshAcknowledgement -> WaitingForAcknowledgementStateScreen(onEvent)
                }
            }
        )
    }
}

@Composable
fun AppHeader(onSettingsClick: () -> Unit, onHistoryClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Title at the top center
        Text(
            text = "Eye Refresh",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
        
        // History button at the top left, aligned with the title
        IconButton(
            onClick = onHistoryClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "History",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Settings button at the top right, aligned with the title
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AppTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun AppMessage(text: String, isPrimary: Boolean = false) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 32.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun CountdownDisplay(remainingTime: Long) {
    Text(
        text = formatCountdown(remainingTime),
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun LiveCountdownDisplay(
    endTime: Long,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    val remainingTime = maxOf(0L, endTime - currentTime)
    
    CountdownDisplay(remainingTime)
}

@Composable
fun LiveElapsedDisplay(
    startTime: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    val elapsedTime = currentTime - startTime
    val remainingTime = maxOf(0L, duration - elapsedTime)
    
    CountdownDisplay(remainingTime)
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.width(200.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text)
    }
}

@Composable
fun ActionButtonsRow(
    primaryText: String,
    secondaryText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ActionButton(
            text = primaryText,
            onClick = onPrimaryClick,
            modifier = Modifier.width(120.dp)
        )
        
        ActionButton(
            text = secondaryText,
            onClick = onSecondaryClick,
            modifier = Modifier.width(120.dp)
        )
    }
}

@Composable
fun CenteredContent(
    title: String,
    message: String? = null,
    content: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AppTitle(title)
            
            message?.let { AppMessage(it) }
            
            content?.invoke()
            
            actions?.invoke()
        }
    }
}

@Composable
fun PausedStateScreen(onEvent: (AppEvent) -> Unit) {
    CenteredContent(
        title = "Eye Care Reminders Paused",
        message = "Your eye care reminders are currently paused. Enable them to start protecting your eye health.",
        actions = {
            ActionButton(
                text = "Enable Reminders",
                onClick = { onEvent(AppEvent.SchedulingTurnedOn) }
            )
        }
    )
}

@Composable
fun ScheduledStateScreen(appState: AppState.TimeLeftForNextRefresh, onEvent: (AppEvent) -> Unit) {
    CenteredContent(
        title = "Next Reminder Scheduled",
        content = {
            LiveCountdownDisplay(endTime = appState.scheduledTimeInMillis)
        },
        actions = {
            ActionButton(
                text = "Pause Reminders",
                onClick = { onEvent(AppEvent.SchedulingPaused) }
            )
        }
    )
}

@Composable
fun RefreshCanStartStateScreen(onEvent: (AppEvent) -> Unit) {
    val context = LocalContext.current
    val config = remember { Config.loadFromPreferences(context) }
    val breakDurationSeconds = config.breakDurationMs / 1000
    
    CenteredContent(
        title = "Time for Eye Refresh!",
        message = "Take a ${breakDurationSeconds}-second break to look at something 20 feet away",
        actions = {
            ActionButtonsRow(
                primaryText = "Start Break",
                secondaryText = "Snooze",
                onPrimaryClick = { onEvent(AppEvent.RefreshStarted) },
                onSecondaryClick = { onEvent(AppEvent.SnoozeRequested) }
            )
        }
    )
}

@Composable
fun RefreshStateScreen(appState: AppState.RefreshHappening, onEvent: (AppEvent) -> Unit) {
    val context = LocalContext.current
    val config = remember { Config.loadFromPreferences(context) }
    val breakDurationSeconds = config.breakDurationMs / 1000
    
    CenteredContent(
        title = "Eye Refresh in Progress",
        message = "Look at something 20 feet away for ${breakDurationSeconds} seconds",
        content = {
            LiveElapsedDisplay(
                startTime = appState.startTimeInMillis,
                duration = config.breakDurationMs
            )
        }
        // No action buttons - user must wait for the timer to complete
    )
}

@Composable
fun WaitingForAcknowledgementStateScreen(onEvent: (AppEvent) -> Unit) {
    val context = LocalContext.current
    val config = remember { Config.loadFromPreferences(context) }
    val breakDurationSeconds = config.breakDurationMs / 1000
    
    CenteredContent(
        title = "Eye Refresh Complete!",
        message = "Great job! Your ${breakDurationSeconds}-second eye refresh is complete.",
        actions = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionButton(
                    text = "I did it!",
                    onClick = { onEvent(AppEvent.MarkRefreshCompleted) }
                )
                ActionButton(
                    text = "I couldn't do it",
                    onClick = { onEvent(AppEvent.RefreshCouldNotHappen) }
                )
            }
        }
    )
}

private fun formatTime(timeInMillis: Long): String {
    val hours = (timeInMillis / (1000 * 60 * 60)) % 24
    val minutes = (timeInMillis / (1000 * 60)) % 60
    return String.format("%02d:%02d", hours, minutes)
}

private fun formatCountdown(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    
    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format("%02d:%02d", minutes, seconds)
    }
}