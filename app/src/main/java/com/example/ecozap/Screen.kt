package com.example.ecozap.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object StationList : Screen("station_list")
    object CngStation : Screen("cng_station")
    object Profile : Screen("profile")
    object FuelNeed : Screen("fuel_need")
    object SOS : Screen("sos")
    object VehicleSetup : Screen("vehicle_setup")
    object Reachability : Screen("reachability")
    object StationDetails : Screen("station_details/{stationId}") {
        fun createRoute(stationId: Int) = "station_details/$stationId"
    }
}