package com.sivakar.eyerefresh.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EyeRefreshHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Main header row with back button and app title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // App title (centered, like in main activity)
            Text(
                text = "Eye Refresh",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Empty space to balance the back button
            Spacer(
                modifier = Modifier
                    .size(48.dp)
                    .padding(top = 8.dp)
            )
        }
        
        // Screen-specific title below
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
} 