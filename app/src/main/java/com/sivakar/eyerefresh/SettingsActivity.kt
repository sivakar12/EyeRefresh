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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title at the top
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
                ReminderIntervalDropdown(
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
                BreakDurationDropdown(
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
                SnoozeDurationDropdown(
                    currentValue = config.snoozeDurationMs,
                    onValueChange = { newValue ->
                        val newConfig = config.copy(snoozeDurationMs = newValue)
                        config = newConfig
                        Config.saveToPreferences(context, newConfig)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Extended snooze duration setting
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Extended Snooze Duration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExtendedSnoozeDurationDropdown(
                    currentValue = config.extendedSnoozeDurationMs,
                    onValueChange = { newValue ->
                        val newConfig = config.copy(extendedSnoozeDurationMs = newValue)
                        config = newConfig
                        Config.saveToPreferences(context, newConfig)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Reset to defaults button at the bottom
        Button(
            onClick = {
                val defaultConfig = Config.getDefault()
                config = defaultConfig
                Config.saveToPreferences(context, defaultConfig)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset to Defaults")
        }
    }
}

@Composable
fun ReminderIntervalDropdown(
    currentValue: Long,
    onValueChange: (Long) -> Unit
) {
    val entries = stringArrayResource(R.array.reminder_interval_entries)
    val values = stringArrayResource(R.array.reminder_interval_values)
    
    var expanded by remember { mutableStateOf(false) }
    val currentIndex = values.indexOf((currentValue / 1000).toString())
    val displayText = if (currentIndex >= 0) entries[currentIndex] else "20 seconds"
    
    Box {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Close dropdown" else "Open dropdown"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            entries.forEachIndexed { index, entry ->
                DropdownMenuItem(
                    text = { Text(entry) },
                    onClick = {
                        val valueInSeconds = values[index].toLong()
                        onValueChange(valueInSeconds * 1000)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BreakDurationDropdown(
    currentValue: Long,
    onValueChange: (Long) -> Unit
) {
    val entries = stringArrayResource(R.array.break_duration_entries)
    val values = stringArrayResource(R.array.break_duration_values)
    
    var expanded by remember { mutableStateOf(false) }
    val currentIndex = values.indexOf((currentValue / 1000).toString())
    val displayText = if (currentIndex >= 0) entries[currentIndex] else "20 seconds"
    
    Box {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Close dropdown" else "Open dropdown"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            entries.forEachIndexed { index, entry ->
                DropdownMenuItem(
                    text = { Text(entry) },
                    onClick = {
                        val valueInSeconds = values[index].toLong()
                        onValueChange(valueInSeconds * 1000)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SnoozeDurationDropdown(
    currentValue: Long,
    onValueChange: (Long) -> Unit
) {
    val entries = stringArrayResource(R.array.snooze_duration_entries)
    val values = stringArrayResource(R.array.snooze_duration_values)
    
    var expanded by remember { mutableStateOf(false) }
    val currentIndex = values.indexOf((currentValue / 1000).toString())
    val displayText = if (currentIndex >= 0) entries[currentIndex] else "2 minutes"
    
    Box {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Close dropdown" else "Open dropdown"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            entries.forEachIndexed { index, entry ->
                DropdownMenuItem(
                    text = { Text(entry) },
                    onClick = {
                        val valueInSeconds = values[index].toLong()
                        onValueChange(valueInSeconds * 1000)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExtendedSnoozeDurationDropdown(
    currentValue: Long,
    onValueChange: (Long) -> Unit
) {
    val entries = stringArrayResource(R.array.extended_snooze_duration_entries)
    val values = stringArrayResource(R.array.extended_snooze_duration_values)
    
    var expanded by remember { mutableStateOf(false) }
    val currentIndex = values.indexOf((currentValue / 1000).toString())
    val displayText = if (currentIndex >= 0) entries[currentIndex] else "5 minutes"
    
    Box {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Close dropdown" else "Open dropdown"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            entries.forEachIndexed { index, entry ->
                DropdownMenuItem(
                    text = { Text(entry) },
                    onClick = {
                        val valueInSeconds = values[index].toLong()
                        onValueChange(valueInSeconds * 1000)
                        expanded = false
                    }
                )
            }
        }
    }
} 