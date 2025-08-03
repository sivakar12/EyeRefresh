package com.sivakar.eyerefresh.settings

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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.tooling.preview.Preview
import com.sivakar.eyerefresh.core.Config
import com.sivakar.eyerefresh.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = viewModel()
    val config by settingsViewModel.config.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Reminder interval setting
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "Reminder Interval",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                ReminderIntervalPills(
                    currentValue = config.reminderIntervalMs,
                    onValueChange = { newValue ->
                        settingsViewModel.updateReminderInterval(newValue)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Break duration setting
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "Break Duration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                BreakDurationPills(
                    currentValue = config.breakDurationMs,
                    onValueChange = { newValue ->
                        settingsViewModel.updateBreakDuration(newValue)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Snooze duration setting
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "Snooze Duration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                SnoozeDurationPills(
                    currentValue = config.snoozeDurationMs,
                    onValueChange = { newValue ->
                        settingsViewModel.updateSnoozeDuration(newValue)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Reset to defaults button
        OutlinedButton(
            onClick = { settingsViewModel.resetToDefaults() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset to Defaults")
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
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface
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
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface
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
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SettingsScreen()
        }
    }
} 