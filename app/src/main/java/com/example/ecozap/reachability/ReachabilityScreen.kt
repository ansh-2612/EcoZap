package com.example.ecozap.reachability

import android.Manifest
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.ecozap.Station
import com.example.ecozap.getCurrentLocationOnce
import com.example.ecozap.loadStationsFromAssets
import com.example.ecozap.ui.theme.DangerRed
import com.example.ecozap.ui.theme.NeonCyan
import com.example.ecozap.ui.theme.NeonGreen
import com.example.ecozap.ui.theme.Night800
import com.example.ecozap.ui.theme.Night900
import com.example.ecozap.ui.theme.TextDim
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReachabilityScreen(navController: NavController) {
    val context = LocalContext.current
    val profile = loadVehicleProfile(context)

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val coroutineScope = rememberCoroutineScope()

    var currentPercent by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var errorText by remember { mutableStateOf("") }
    var hasChecked by remember { mutableStateOf(false) }

    val stations = remember {
        loadStationsFromAssets(context, "delhi_stations.json")
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                coroutineScope.launch {
                    userLocation = getCurrentLocationOnce(fusedClient)
                }
            } else {
                errorText = "Location permission is needed to calculate station distance"
            }
        }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        containerColor = Night900,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reachability Checker",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Night800
                )
            )
        }
    ) { paddingValues ->

        if (profile == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No vehicle profile found",
                    color = Color.White
                )

                Button(
                    onClick = {
                        navController.navigate("vehicle_setup")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Setup Vehicle", color = Color.Black)
                }
            }
            return@Scaffold
        }

        val percent = currentPercent.toIntOrNull() ?: 0
        val availableRange = profile.fullRangeKm * (percent / 100.0)
        val safeRange = (availableRange - profile.safetyBufferKm).coerceAtLeast(0.0)

        val filteredStations = stations.filter {
            it.type.equals(profile.vehicleType, ignoreCase = true)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Night800)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (profile.vehicleType == "EV")
                            Icons.Default.ElectricCar
                        else
                            Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = if (profile.vehicleType == "EV") NeonGreen else NeonCyan
                    )

                    Text(
                        text = profile.vehicleName,
                        color = Color.White
                    )

                    Text(
                        text = "Type: ${profile.vehicleType}",
                        color = TextDim
                    )

                    Text(
                        text = "Full Range: ${profile.fullRangeKm} km",
                        color = TextDim
                    )

                    Text(
                        text = "Safety Buffer: ${profile.safetyBufferKm} km",
                        color = TextDim
                    )

                    Button(
                        onClick = {
                            navController.navigate("vehicle_setup")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Edit Vehicle", color = Color.Black)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Night800)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = currentPercent,
                        onValueChange = {
                            currentPercent = it.filter { ch -> ch.isDigit() }
                            errorText = ""
                            hasChecked = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                if (profile.vehicleType == "EV")
                                    "Current Battery %"
                                else
                                    "Current Fuel %"
                            )
                        },
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            when {
                                percent <= 0 || percent > 100 -> {
                                    errorText = "Enter a value between 1 and 100"
                                }

                                userLocation == null -> {
                                    userLocation = Location("fallback").apply {
                                        latitude = 20.3536
                                        longitude = 85.8192
                                    }
                                    errorText = ""
                                    hasChecked = true
                                }

                                else -> {
                                    errorText = ""
                                    hasChecked = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Check Reachability", color = Color.Black)
                    }

                    if (errorText.isNotEmpty()) {
                        Text(errorText, color = DangerRed)
                    }

                    if (hasChecked) {
                        Text(
                            text = "Available Range: ${String.format("%.1f", availableRange)} km",
                            color = NeonGreen
                        )

                        Text(
                            text = "Safe Range: ${String.format("%.1f", safeRange)} km",
                            color = NeonCyan
                        )
                    }
                }
            }

            if (hasChecked && userLocation != null) {
                Text(
                    text = "Nearby ${profile.vehicleType} Stations",
                    color = Color.White
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredStations) { station ->
                        ReachabilityStationCard(
                            station = station,
                            userLocation = userLocation!!,
                            availableRange = availableRange,
                            safeRange = safeRange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReachabilityStationCard(
    station: Station,
    userLocation: Location,
    availableRange: Double,
    safeRange: Double
) {
    val result = FloatArray(1)

    Location.distanceBetween(
        userLocation.latitude,
        userLocation.longitude,
        station.lat,
        station.lng,
        result
    )

    val distanceKm = result[0] / 1000.0

    val status = when {
        distanceKm <= safeRange -> "Reachable"
        distanceKm <= availableRange -> "Risky"
        else -> "Unreachable"
    }

    val statusColor = when (status) {
        "Reachable" -> NeonGreen
        "Risky" -> NeonCyan
        else -> DangerRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Night800)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = station.name,
                color = Color.White
            )

            Text(
                text = "Distance: ${String.format("%.1f", distanceKm)} km",
                color = TextDim
            )

            Text(
                text = status,
                color = statusColor
            )

            if (status == "Risky") {
                Text(
                    text = "You may reach, but it is close to your safety buffer.",
                    color = NeonCyan
                )
            }

            if (status == "Unreachable") {
                Text(
                    text = "Not recommended with current range.",
                    color = DangerRed
                )
            }
        }
    }
}

