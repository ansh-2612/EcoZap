package com.example.ecozap

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.ecozap.ui.navigation.Screen // ✅ uses your custom Screen class
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ecozap.ui.home.HomeScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { HomeScreen(navController) }

        // ✅ Add CngStationScreen with default null values
        composable(Screen.StationList.route) {
            CngStationScreen(navController)   // no need to pass nulls
        }

        composable(Screen.Profile.route) { /* ProfileScreen(navController) */ }
    }
}
