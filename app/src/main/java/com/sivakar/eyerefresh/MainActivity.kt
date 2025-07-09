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
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sivakar.eyerefresh.core.AppEvent
import com.sivakar.eyerefresh.core.AppState
import com.sivakar.eyerefresh.core.Config
import com.sivakar.eyerefresh.ui.theme.EyeRefreshTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
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
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    
    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        showNotificationPermissionDialog = false
    }
    
    // Check notification permission on first launch
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
                    showNotificationPermissionDialog = true
                }
                else -> {
                    // Request permission directly
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    // Permission rationale dialog
    if (showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationPermissionDialog = false },
            title = { Text("Notification Permission") },
            text = { 
                Text("Eye Refresh needs notification permission to send you eye care reminders. This helps protect your eye health.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationPermissionDialog = false
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNotificationPermissionDialog = false }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with title, history, and settings
        AppHeader(
            onSettingsClick = {
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
            },
            onHistoryClick = {
                val intent = Intent(context, HistoryActivity::class.java)
                context.startActivity(intent)
            }
        )
        
        // Main content in the center
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (appState) {
                is AppState.Paused -> PausedStateScreen(onEvent)
                is AppState.TimeLeftForNextRefresh -> ScheduledStateScreen(appState, onEvent)
                is AppState.RefreshCanStart -> RefreshCanStartStateScreen(onEvent)
                is AppState.RefreshHappening -> RefreshStateScreen(appState, onEvent)
                is AppState.WaitingForRefreshAcknowledgement -> WaitingForAcknowledgementStateScreen(onEvent)
            }
        }
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
                imageVector = Icons.Default.List,
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
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    val remainingTime = maxOf(0L, appState.scheduledTimeInMillis - currentTime)
    
    CenteredContent(
        title = "Next Reminder Scheduled",
        content = {
            CountdownDisplay(remainingTime)
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
    
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }
    
    val elapsedTime = currentTime - appState.startTimeInMillis
    val remainingTime = maxOf(0L, config.breakDurationMs - elapsedTime)
    
    CenteredContent(
        title = "Eye Refresh in Progress",
        message = "Look at something 20 feet away for ${breakDurationSeconds} seconds",
        content = {
            CountdownDisplay(remainingTime)
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

// Preview functions
@Preview(showBackground = true)
@Composable
fun PausedStateScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PausedStateScreen(
                onEvent = { /* Preview only */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduledStateScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ScheduledStateScreen(
                appState = AppState.TimeLeftForNextRefresh(
                    scheduledTimeInMillis = System.currentTimeMillis() + (30 * 60 * 1000) // 30 minutes from now
                ),
                onEvent = { /* Preview only */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RefreshCanStartStateScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RefreshCanStartStateScreen(
                onEvent = { /* Preview only */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RefreshStateScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RefreshStateScreen(
                appState = AppState.RefreshHappening(
                    startTimeInMillis = System.currentTimeMillis() - (10 * 1000) // Started 10 seconds ago
                ),
                onEvent = { /* Preview only */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WaitingForAcknowledgementStateScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WaitingForAcknowledgementStateScreen(
                onEvent = { /* Preview only */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen(
                appState = AppState.TimeLeftForNextRefresh(
                    scheduledTimeInMillis = System.currentTimeMillis() + (30 * 60 * 1000)
                ),
                onEvent = { /* Preview only */ }
            )
        }
    }
}