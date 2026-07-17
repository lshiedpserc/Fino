package com.example.fino.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fino.R
import com.example.fino.ui.theme.Primary
import com.example.fino.ui.theme.PrimaryFixedDim
import com.example.fino.ui.theme.SurfaceContainer

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Goals,
        Screen.Scan,
        Screen.History
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(SurfaceContainer.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) Primary.copy(alpha = 0.1f) else Color.Transparent)
                    .border(
                        if (selected) 1.dp else 0.dp,
                        if (selected) Primary.copy(alpha = 0.3f) else Color.Transparent,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // We'll use simple text for icons for now since material symbols need to be imported or custom paths
                Text(
                    text = if(screen == Screen.Dashboard) "D" else if (screen == Screen.Goals) "G" else if (screen == Screen.Scan) "S" else "H",
                    color = if (selected) PrimaryFixedDim else Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = screen.title,
                    color = if (selected) PrimaryFixedDim else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
