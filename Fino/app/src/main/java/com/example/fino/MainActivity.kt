package com.example.fino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fino.ui.navigation.BottomNavigationBar
import com.example.fino.ui.navigation.Screen
import com.example.fino.ui.theme.FinoTheme
import com.example.fino.ui.theme.Background

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinoTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) },
                    containerColor = Background
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) {
                            com.example.fino.ui.dashboard.DashboardScreen(dao = com.example.fino.data.AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current).appDao(), onNavigateToHistory = { navController.navigate(Screen.History.route) })
                        }
                        composable(Screen.Goals.route) {
                            com.example.fino.ui.goals.GoalsScreen(dao = com.example.fino.data.AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current).appDao())
                        }
                        composable(Screen.Scan.route) {
                            com.example.fino.ui.scan.ScanScreen(dao = com.example.fino.data.AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current).appDao(), onNavigateToHistory = { navController.navigate(Screen.History.route) })
                        }
                        composable(Screen.History.route) {
                            com.example.fino.ui.history.HistoryScreen(dao = com.example.fino.data.AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current).appDao())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = Background) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = title, color = androidx.compose.ui.graphics.Color.White)
        }
    }
}
