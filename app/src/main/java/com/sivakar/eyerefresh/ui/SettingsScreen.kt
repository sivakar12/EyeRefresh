package com.sivakar.eyerefresh.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sivakar.eyerefresh.core.Config
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringArrayResource
import com.sivakar.eyerefresh.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sivakar.eyerefresh.SettingsViewModel

@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = viewModel()
    val config by settingsViewModel.config.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
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