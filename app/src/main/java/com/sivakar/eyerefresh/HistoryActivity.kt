package com.sivakar.eyerefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sivakar.eyerefresh.ui.theme.EyeRefreshTheme
import com.sivakar.eyerefresh.ui.components.EyeRefreshHeader
import java.text.SimpleDateFormat
import java.util.*

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
        
        if (sessions.isEmpty()) {
            // Show empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No completed sessions yet",
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
fun SessionCard(session: CompletedSession) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = dateFormat.format(Date(session.startTime)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Duration: ${session.durationSeconds} seconds",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 