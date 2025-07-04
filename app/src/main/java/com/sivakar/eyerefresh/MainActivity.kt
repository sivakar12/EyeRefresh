package com.sivakar.eyerefresh

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sivakar.eyerefresh.ui.theme.EyeRefreshTheme
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Continue regardless of permission result
        showPermissionDialog = false
    }
    
    // Check permission on first launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                (context as? ComponentActivity)?.shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                ) == true -> {
                    // Show rationale dialog
                    showPermissionDialog = true
                }
                else -> {
                    // Request permission directly
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    // Permission rationale dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Notification Permission") },
            text = { 
                Text("Eye Refresh needs notification permission to send you eye care reminders. This helps protect your eye health.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title at the top
        Text(
            text = "Eye Refresh",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 32.dp)
        )
        
        // Settings button at the top right
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Main content in the center
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (appState) {
                is AppState.RemindersPaused -> PausedStateScreen(onEvent)
                is AppState.ReminderScheduled -> ScheduledStateScreen(appState, onEvent)
                is AppState.ReminderSent -> SentStateScreen(onEvent)
                is AppState.RefreshHappening -> RefreshStateScreen(appState, onEvent)
                is AppState.RefreshComplete -> RefreshCompleteStateScreen(onEvent)
            }
        }
    }
}

@Composable
fun PausedStateScreen(onEvent: (AppEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Eye Care Reminders Paused",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Button(
            onClick = { onEvent(AppEvent.NotificationsTurnedOn) },
            modifier = Modifier.width(200.dp)
        ) {
            Text("Enable Reminders")
        }
    }
}

@Composable
fun ScheduledStateScreen(appState: AppState.ReminderScheduled, onEvent: (AppEvent) -> Unit) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    val remainingTime = maxOf(0L, appState.timeInMillis - currentTime)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Next Reminder Scheduled",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Time: ${formatTime(appState.timeInMillis)}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Time remaining: ${formatCountdown(remainingTime)}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Button(
            onClick = { onEvent(AppEvent.NotificationsPaused) },
            modifier = Modifier.width(200.dp)
        ) {
            Text("Pause Reminders")
        }
    }
}

@Composable
fun SentStateScreen(onEvent: (AppEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Time for Eye Refresh!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Take a 20-second break to look at something 20 feet away",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onEvent(AppEvent.RefreshStarted) },
                modifier = Modifier.width(120.dp)
            ) {
                Text("Start Break")
            }
            
            Button(
                onClick = { onEvent(AppEvent.RefreshAbandoned) },
                modifier = Modifier.width(120.dp)
            ) {
                Text("Skip")
            }
        }
    }
}

@Composable
fun RefreshStateScreen(appState: AppState.RefreshHappening, onEvent: (AppEvent) -> Unit) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    val breakDurationMs = 20 * 1000L // This should come from config
    val elapsedTime = currentTime - appState.startTimeInMillis
    val remainingTime = maxOf(0L, breakDurationMs - elapsedTime)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Eye Refresh in Progress",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Look at something 20 feet away for 20 seconds",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Text(
            text = "Time remaining: ${formatCountdown(remainingTime)}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onEvent(AppEvent.RefreshMarkedComplete) },
                modifier = Modifier.width(120.dp)
            ) {
                Text("Complete")
            }
            
            Button(
                onClick = { onEvent(AppEvent.RefreshAbandoned) },
                modifier = Modifier.width(120.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun RefreshCompleteStateScreen(onEvent: (AppEvent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Eye Refresh Complete!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Great job! Your 20-second eye refresh is complete.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onEvent(AppEvent.RefreshMarkedComplete) },
                modifier = Modifier.width(120.dp)
            ) {
                Text("Complete")
            }
            
            Button(
                onClick = { onEvent(AppEvent.RefreshAbandoned) },
                modifier = Modifier.width(120.dp)
            ) {
                Text("Skip")
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