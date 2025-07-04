package com.sivakar.eyerefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sivakar.eyerefresh.ui.theme.EyeRefreshTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EyeRefreshTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val appState by viewModel.appState.collectAsState()

                    MainScreen(appState, viewModel::onEvent)
                }
            }
        }
    }
}



@Composable
fun MainScreen(appState: AppState, onEvent: (AppEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Eye Refresh",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        when (appState) {
            is AppState.RemindersPaused -> PausedStateScreen(onEvent)
            is AppState.ReminderScheduled -> ScheduledStateScreen(appState, onEvent)
            is AppState.ReminderSent -> SentStateScreen(onEvent)
            is AppState.RefreshHappening -> RefreshStateScreen(onEvent)
        }
    }
}

@Composable
fun PausedStateScreen(onEvent: (AppEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Eye Care Reminders Paused",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Button(
            onClick = { onEvent(AppEvent.NotificationsTurnedOn) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enable Reminders")
        }
    }
}

@Composable
fun ScheduledStateScreen(appState: AppState.ReminderScheduled, onEvent: (AppEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Next Reminder Scheduled",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Time: ${formatTime(appState.timeInMillis)}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Countdown timer
        Text(
            text = "Time remaining: ${formatCountdown(appState.countdownMillis)}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Button(
            onClick = { onEvent(AppEvent.NotificationsPaused) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pause Reminders")
        }
    }
}

@Composable
fun SentStateScreen(onEvent: (AppEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Time for Eye Refresh!",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Take a 20-second break to look at something 20 feet away",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onEvent(AppEvent.RefreshStarted) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Start Break")
            }
            
            Button(
                onClick = { onEvent(AppEvent.RefreshAbandoned) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Skip")
            }
        }
    }
}

@Composable
fun RefreshStateScreen(onEvent: (AppEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Eye Refresh in Progress",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Look at something 20 feet away for 20 seconds",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onEvent(AppEvent.RefreshMarkedComplete) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Complete")
            }
            
            Button(
                onClick = { onEvent(AppEvent.RefreshAbandoned) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
        }
    }
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