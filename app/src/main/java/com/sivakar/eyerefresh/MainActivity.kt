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
            .padding(16.dp)
    ) {
        // Header with title and settings
        AppHeader(
            onSettingsClick = {
                val intent = Intent(context, SettingsActivity::class.java)
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
fun AppHeader(onSettingsClick: () -> Unit) {
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
                onClick = { onEvent(AppEvent.NotificationsTurnedOn) }
            )
        }
    )
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
    
    CenteredContent(
        title = "Next Reminder Scheduled",
        content = {
            CountdownDisplay(remainingTime)
        },
        actions = {
            ActionButton(
                text = "Pause Reminders",
                onClick = { onEvent(AppEvent.NotificationsPaused) }
            )
        }
    )
}

@Composable
fun SentStateScreen(onEvent: (AppEvent) -> Unit) {
    val context = LocalContext.current
    val config = remember { Config.loadFromPreferences(context) }
    val breakDurationSeconds = config.breakDurationMs / 1000
    
    CenteredContent(
        title = "Time for Eye Refresh!",
        message = "Take a ${breakDurationSeconds}-second break to look at something 20 feet away",
        actions = {
            ActionButtonsRow(
                primaryText = "Start Break",
                secondaryText = "Skip",
                onPrimaryClick = { onEvent(AppEvent.RefreshStarted) },
                onSecondaryClick = { onEvent(AppEvent.RefreshAbandoned) }
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
        },
        actions = {
            ActionButtonsRow(
                primaryText = "Complete",
                secondaryText = "Cancel",
                onPrimaryClick = { onEvent(AppEvent.RefreshMarkedComplete) },
                onSecondaryClick = { onEvent(AppEvent.RefreshAbandoned) }
            )
        }
    )
}

@Composable
fun RefreshCompleteStateScreen(onEvent: (AppEvent) -> Unit) {
    val context = LocalContext.current
    val config = remember { Config.loadFromPreferences(context) }
    val breakDurationSeconds = config.breakDurationMs / 1000
    
    CenteredContent(
        title = "Eye Refresh Complete!",
        message = "Great job! Your ${breakDurationSeconds}-second eye refresh is complete.",
        actions = {
            ActionButtonsRow(
                primaryText = "Complete",
                secondaryText = "Skip",
                onPrimaryClick = { onEvent(AppEvent.RefreshMarkedComplete) },
                onSecondaryClick = { onEvent(AppEvent.RefreshAbandoned) }
            )
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
                appState = AppState.ReminderScheduled(
                    timeInMillis = System.currentTimeMillis() + (30 * 60 * 1000) // 30 minutes from now
                ),
                onEvent = { /* Preview only */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SentStateScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SentStateScreen(
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
fun RefreshCompleteStateScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RefreshCompleteStateScreen(
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
                appState = AppState.ReminderScheduled(
                    timeInMillis = System.currentTimeMillis() + (30 * 60 * 1000)
                ),
                onEvent = { /* Preview only */ }
            )
        }
    }
}