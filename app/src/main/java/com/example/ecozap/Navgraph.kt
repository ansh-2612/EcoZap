package com.example.ecozap

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.ecozap.ui.navigation.Screen // ✅ uses your custom Screen class
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ecozap.ui.home.HomeScreen
import com.example.ecozap.reachability.VehicleSetupScreen
import com.example.ecozap.reachability.ReachabilityScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) { HomeScreen(navController) }

        composable(Screen.StationList.route) {
            CngStationScreen(navController)
        }

        composable(Screen.StationDetails.route) { backStackEntry ->

            val stationId =
                backStackEntry.arguments?.getString("stationId")?.toIntOrNull()

            StationDetailsScreen(
                navController = navController,
                stationId = stationId
            )
        }
        composable(Screen.VehicleSetup.route) {
            VehicleSetupScreen(navController)
        }

        composable(Screen.Reachability.route) {
            ReachabilityScreen(navController)
        }

        composable(Screen.Profile.route) { }
    }

}
