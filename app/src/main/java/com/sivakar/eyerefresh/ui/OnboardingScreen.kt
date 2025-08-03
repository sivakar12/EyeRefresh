package com.sivakar.eyerefresh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBackground: Color
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val steps = listOf(
        OnboardingStep(
            title = "Welcome to Eye Refresh",
            description = "Take care of your eyes with regular breaks to reduce eye strain and maintain healthy vision.",
            icon = Icons.Default.Favorite,
            iconBackground = MaterialTheme.colorScheme.primary
        ),
        OnboardingStep(
            title = "Smart Reminders",
            description = "Get gentle notifications every 20 minutes to remind you to take a break from your screen.",
            icon = Icons.Default.Notifications,
            iconBackground = MaterialTheme.colorScheme.secondary
        ),
        OnboardingStep(
            title = "Quick Breaks",
            description = "Take 20-second breaks to look away from your screen and focus on distant objects.",
            icon = Icons.Default.Info,
            iconBackground = MaterialTheme.colorScheme.tertiary
        ),
        OnboardingStep(
            title = "Track Your Progress",
            description = "View your break history and see how well you're taking care of your eyes over time.",
            icon = Icons.Default.List,
            iconBackground = MaterialTheme.colorScheme.primary
        ),
        OnboardingStep(
            title = "Customizable Settings",
            description = "Adjust reminder intervals, break durations, and notification preferences to suit your needs.",
            icon = Icons.Default.Settings,
            iconBackground = MaterialTheme.colorScheme.secondary
        )
    )

    var currentStep by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Progress indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step ${currentStep + 1} of ${steps.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(steps.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index <= currentStep) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(steps[currentStep].iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = steps[currentStep].icon,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = steps[currentStep].title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Description
                Text(
                    text = steps[currentStep].description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Next/Complete button
            Button(
                onClick = {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (currentStep < steps.size - 1) "Next" else "Get Started"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (currentStep < steps.size - 1) 
                        Icons.Default.ArrowForward 
                    else 
                        Icons.Default.Check,
                    contentDescription = if (currentStep < steps.size - 1) "Next" else "Complete",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Skip button
        if (currentStep < steps.size - 1) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    "Skip Onboarding",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 