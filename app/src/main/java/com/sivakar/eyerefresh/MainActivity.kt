package com.sivakar.eyerefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.core.view.WindowCompat
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import com.sivakar.eyerefresh.core.AppConfig
import com.sivakar.eyerefresh.NotificationPermissionHandler
import com.sivakar.eyerefresh.settings.SettingsScreen
import com.sivakar.eyerefresh.history.HistoryScreen
import com.sivakar.eyerefresh.MainScreen
import com.sivakar.eyerefresh.OnboardingScreen
import com.sivakar.eyerefresh.AboutScreen
import com.sivakar.eyerefresh.EyeRefreshTheme

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

// Data class for drawer items
data class DrawerItem(val label: String, val route: String, val icon: ImageVector)

// Configuration object for drawer items
object DrawerItems {
    val items = listOf(
        DrawerItem("Home", "main", Icons.Default.Home),
        DrawerItem("History", "history", Icons.Default.List),
        DrawerItem("Settings", "settings", Icons.Default.Settings),
        DrawerItem("Onboarding", "onboarding", Icons.Default.Info),
        DrawerItem("About", "about", Icons.Default.Person)
    )
}

// Main drawer composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    drawerItems: List<DrawerItem> = DrawerItems.items,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(24.dp))
        drawerItems.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                icon = { Icon(item.icon, contentDescription = item.label) },
                onClick = {
                    onItemClick(item.route)
                    onClose()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

// TopAppBar composable for the app
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentRoute: String?,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                text = getRouteTitle(currentRoute),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (currentRoute == "main") {
                // Show hamburger menu for main screen
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            } else {
                // Show back button for other screens
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}

// Helper function to get title for each route
private fun getRouteTitle(route: String?): String {
    return when (route) {
        "main" -> "EyeRefresh"
        "history" -> "History"
        "settings" -> "Settings"
        "about" -> "About"
        "onboarding" -> "Onboarding"
        else -> "EyeRefresh"
    }
}

// App navigation composable
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    showTopBar: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("main") {
            MainScreen()
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
                    navController.navigate("main") {
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
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // Determine start destination based on onboarding status
                val startDestination = if (AppConfig.shouldShowOnboarding(this)) {
                    "onboarding"
                } else {
                    "main"
                }

                // Handle notification permissions
                NotificationPermissionHandler()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(
                            currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route,
                            onItemClick = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            },
                            onClose = {
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                    
                    // Single NavHost with conditional TopAppBar
                    if (currentRoute == "onboarding") {
                        // No TopAppBar for onboarding
                        AppNavigation(
                            navController = navController,
                            startDestination = startDestination,
                            showTopBar = false
                        )
                    } else {
                        // TopAppBar for all other screens
                        Scaffold(
                            topBar = {
                                AppTopBar(
                                    currentRoute = currentRoute,
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onBackClick = { 
                                        navController.navigate("main") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        ) { paddingValues ->
                            AppNavigation(
                                navController = navController,
                                startDestination = startDestination,
                                showTopBar = true,
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                    }
                }
            }
        }
    }
}




