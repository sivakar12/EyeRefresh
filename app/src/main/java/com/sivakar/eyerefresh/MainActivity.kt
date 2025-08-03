package com.sivakar.eyerefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sivakar.eyerefresh.core.AppState
import com.sivakar.eyerefresh.core.AppConfig
import com.sivakar.eyerefresh.NotificationPermissionHandler
import com.sivakar.eyerefresh.ui.SettingsScreen
import com.sivakar.eyerefresh.ui.HistoryScreen
import com.sivakar.eyerefresh.ui.HomeScreen
import com.sivakar.eyerefresh.ui.OnboardingScreen
import com.sivakar.eyerefresh.ui.AboutScreen
import com.sivakar.eyerefresh.ui.theme.EyeRefreshTheme

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Run health check when main activity opens
        EventManager.getInstance(this).recoverState("app_launch")
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            EyeRefreshTheme {
                val mainViewModel: MainViewModel = viewModel()
                val appState by mainViewModel.appState.collectAsState()
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // Determine start destination based on onboarding status
                val startDestination = if (AppConfig.shouldShowOnboarding(this)) {
                    "onboarding"
                } else {
                    "home"
                }

                // Handle notification permissions
                NotificationPermissionHandler()

                val drawerItems = listOf(
                    DrawerItem("Home", "home", Icons.Default.Home),
                    DrawerItem("History", "history", Icons.Default.List),
                    DrawerItem("Settings", "settings", Icons.Default.Settings),
                    DrawerItem("Onboarding", "onboarding", Icons.Default.Info),
                    DrawerItem("About", "about", Icons.Default.Person)
                )

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Spacer(Modifier.height(24.dp))
                            drawerItems.forEach { item ->
                                NavigationDrawerItem(
                                    label = { Text(item.label) },
                                    selected = navController.currentBackStackEntryAsState().value?.destination?.route == item.route,
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    onClick = {
                                        navController.navigate(item.route) {
                                            launchSingleTop = true
                                        }
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                    }
                ) {
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                    if (currentRoute == "onboarding") {
                        // No TopAppBar for onboarding
                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable("home") {
                                HomeScreen(appState = appState, onEvent = mainViewModel::onEvent)
                            }
                            composable("history") {
                                HistoryScreen()
                            }
                            composable("settings") {
                                SettingsScreen()
                            }
                            composable("onboarding") {
                                val context = LocalContext.current
                                OnboardingScreen(
                                    onComplete = {
                                        AppConfig.markOnboardingCompleted(context)
                                        navController.navigate("home") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("about") {
                                AboutScreen()
                            }
                        }
                    } else {
                        // TopAppBar for all other screens
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { 
                                        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                                        Text(
                                            text = when (currentRoute) {
                                                "home" -> "EyeRefresh"
                                                "history" -> "History"
                                                "settings" -> "Settings"
                                                "about" -> "About"
                                                "onboarding" -> "Onboarding"
                                                else -> "EyeRefresh"
                                            },
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    navigationIcon = {
                                        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                                        if (currentRoute == "home") {
                                            // Show hamburger menu for home screen
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                                            }
                                        } else {
                                            // Show back button for other screens
                                            IconButton(onClick = { 
                                                navController.navigate("home") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }) {
                                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                            }
                                        }
                                    }
                                )
                            }
                        ) { paddingValues ->
                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                                modifier = Modifier.padding(paddingValues)
                            ) {
                                composable("home") {
                                    HomeScreen(appState = appState, onEvent = mainViewModel::onEvent)
                                }
                                composable("history") {
                                    HistoryScreen()
                                }
                                composable("settings") {
                                    SettingsScreen()
                                }
                                composable("onboarding") {
                                    val context = LocalContext.current
                                    OnboardingScreen(
                                        onComplete = {
                                            AppConfig.markOnboardingCompleted(context)
                                            navController.navigate("home") {
                                                popUpTo("onboarding") { inclusive = true }
                                            }
                                        }
                                    )
                                }
                                composable("about") {
                                    AboutScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper data class for drawer items
private data class DrawerItem(val label: String, val route: String, val icon: ImageVector)


