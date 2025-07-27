package com.sivakar.eyerefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sivakar.eyerefresh.ui.theme.EyeRefreshTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.tooling.preview.Preview
import com.sivakar.eyerefresh.core.Config

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EyeRefreshTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var config by remember { mutableStateOf(Config.loadFromPreferences(context)) }
    
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title at the top center
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
                
                // Back button at the top left
                IconButton(
                    onClick = { (context as? ComponentActivity)?.finish() },
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
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Reminder interval setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Reminder Interval",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReminderIntervalPills(
                        currentValue = config.reminderIntervalMs,
                        onValueChange = { newValue ->
                            val newConfig = config.copy(reminderIntervalMs = newValue)
                            config = newConfig
                            Config.saveToPreferences(context, newConfig)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Break duration setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Break Duration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BreakDurationPills(
                        currentValue = config.breakDurationMs,
                        onValueChange = { newValue ->
                            val newConfig = config.copy(breakDurationMs = newValue)
                            config = newConfig
                            Config.saveToPreferences(context, newConfig)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Snooze duration setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Snooze Duration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SnoozeDurationPills(
                        currentValue = config.snoozeDurationMs,
                        onValueChange = { newValue ->
                            val newConfig = config.copy(snoozeDurationMs = newValue)
                            config = newConfig
                            Config.saveToPreferences(context, newConfig)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Reset to defaults button
            OutlinedButton(
                onClick = {
                    val defaultConfig = Config.getDefault()
                    config = defaultConfig
                    Config.saveToPreferences(context, defaultConfig)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to Defaults")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Done button
            Button(
                onClick = { (context as? ComponentActivity)?.finish() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun ReminderIntervalPills(
    currentValue: Long,
    onValueChange: (Long) -> Unit
) {
    val entries = stringArrayResource(R.array.reminder_interval_entries)
    val values = stringArrayResource(R.array.reminder_interval_values)
    
    val currentIndex = values.indexOf((currentValue / 1000).toString())
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries.size) { index ->
            val isSelected = index == currentIndex
            val entry = entries[index]
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    val valueInSeconds = values[index].toLong()
                    onValueChange(valueInSeconds * 1000)
                },
                label = {
                    Text(
                        entry,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun BreakDurationPills(
    currentValue: Long,
    onValueChange: (Long) -> Unit
) {
    val entries = stringArrayResource(R.array.break_duration_entries)
    val values = stringArrayResource(R.array.break_duration_values)
    
    val currentIndex = values.indexOf((currentValue / 1000).toString())
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries.size) { index ->
            val isSelected = index == currentIndex
            val entry = entries[index]
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    val valueInSeconds = values[index].toLong()
                    onValueChange(valueInSeconds * 1000)
                },
                label = {
                    Text(
                        entry,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun SnoozeDurationPills(
    currentValue: Long,
    onValueChange: (Long) -> Unit
) {
    val entries = stringArrayResource(R.array.snooze_duration_entries)
    val values = stringArrayResource(R.array.snooze_duration_values)
    
    val currentIndex = values.indexOf((currentValue / 1000).toString())
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries.size) { index ->
            val isSelected = index == currentIndex
            val entry = entries[index]
            
            FilterChip(
                selected = isSelected,
                onClick = {
                    val valueInSeconds = values[index].toLong()
                    onValueChange(valueInSeconds * 1000)
                },
                label = {
                    Text(
                        entry,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    EyeRefreshTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SettingsScreen()
        }
    }
} 