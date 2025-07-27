package com.sivakar.eyerefresh

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sivakar.eyerefresh.core.AppState
import com.sivakar.eyerefresh.ui.MainScreen
import com.sivakar.eyerefresh.NotificationPermissionHandler

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Run health check when main activity opens
        EventManager.getInstance(this).recoverState("app_launch")
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val viewModel: MainViewModel = viewModel()
            val appState by viewModel.appState.collectAsState()

            // Handle notification permissions
            NotificationPermissionHandler()
            
            MainScreen(
                appState = appState,
                onEvent = viewModel::onEvent,
                handleNavigation = { location ->
                    when (location) {
                        "settings" -> {
                            val intent = Intent(this, SettingsActivity::class.java)
                            startActivity(intent)
                        }
                        "history" -> {
                            val intent = Intent(this, com.sivakar.eyerefresh.history.HistoryActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            )
        }
    }
}


