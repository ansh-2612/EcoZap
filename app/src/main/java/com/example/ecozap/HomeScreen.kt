package com.example.ecozap.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.ecozap.ActionCard
import com.example.ecozap.MapWithLocation
import com.example.ecozap.PriceCard
import com.example.ecozap.Station
import com.example.ecozap.StationCard
import com.example.ecozap.StatusPill
import com.example.ecozap.findNearestStation
import com.example.ecozap.getCurrentLocationOnce
import com.example.ecozap.loadStationsFromAssets
import com.example.ecozap.ui.navigation.Screen
import com.example.ecozap.ui.theme.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import com.example.ecozap.reachability.loadVehicleProfile
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
@Composable
fun HomeScreen(nav: NavController) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val stations = remember { loadStationsFromAssets(context, "delhi_stations.json") }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var nearestPair by remember { mutableStateOf<Pair<Station, Float>?>(null) }
    var loadingLocation by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasPermission = granted
            if (granted) {
                coroutineScope.launch {
                    loadingLocation = true
                    userLocation = getCurrentLocationOnce(fusedClient)
                    val loc = userLocation
                    nearestPair = if (loc != null) {
                        findNearestStation(loc.latitude, loc.longitude, stations)
                    } else {
                        null
                    }
                    loadingLocation = false
                }
            }
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            loadingLocation = true
            userLocation = getCurrentLocationOnce(fusedClient)
            val loc = userLocation
            nearestPair = if (loc != null) {
                findNearestStation(loc.latitude, loc.longitude, stations)
            } else {
                null
            }
            loadingLocation = false
        }
    }

    val nearestStation = nearestPair?.first
    val nearestDistanceKm = nearestPair?.second?.div(1000f)

    val distanceText = if (nearestDistanceKm != null) {
        String.format("%.1f km", nearestDistanceKm)
    } else {
        "N/A"
    }

    val availableSlotCount = nearestStation?.slots?.count { it.available } ?: 0

    val slotsText = when {
        nearestStation == null -> "No station"
        availableSlotCount > 0 -> "$availableSlotCount slots available"
        else -> "Fully booked"
    }

    val nearestEv = stations.firstOrNull { it.type.equals("EV", true) }
    val nearestCng = stations.firstOrNull { it.type.equals("CNG", true) }

    Scaffold(
        containerColor = Night900,
        bottomBar = { BottomNav(nav, context) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Welcome to EcoZap",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PriceCard(
                    title = "CNG Price",
                    price = "₹ 78.50/kg",
                    icon = {
                        Icon(
                            Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = NeonGreen
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                PriceCard(
                    title = "EV Charging",
                    price = "₹ 12.00/unit",
                    icon = {
                        Icon(
                            Icons.Default.ElectricCar,
                            contentDescription = null,
                            tint = NeonCyan
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = if (loadingLocation) "Finding nearby station..." else "Updated just now",
                color = TextDim,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = "EV station",
                    icon = {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            tint = NeonGreen
                        )
                    },
                    modifier = Modifier.weight(1f),
                    onClick = {
                        nav.navigate(Screen.StationList.route)
                    }
                )

                ActionCard(
                    title = "CNG Filling Station",
                    icon = {
                        Icon(
                            Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = NeonCyan
                        )
                    },
                    modifier = Modifier.weight(1f),
                    onClick = {
                        nav.navigate(Screen.StationList.route)
                    }
                )
            }

            ActionCard(
                title = "Fuel Need",
                icon = {
                    Icon(
                        Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = NeonGreen
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val profile = loadVehicleProfile(context)
                    if (profile == null) {
                        nav.navigate(Screen.VehicleSetup.route)
                    } else {
                        nav.navigate(Screen.Reachability.route)
                    }
                }
            )

            Text(
                text = "Nearby Stations",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            StationCard(
                name = nearestStation?.name ?: "No nearby station found",
                distanceText = distanceText,
                slotsText = slotsText,
                onBook = {
                    nav.navigate(Screen.StationList.route)
                }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Night800),
                shape = MaterialTheme.shapes.medium
            ) {
                MapWithLocation(
                    stations = stations,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatusPill(
                    labelTop = "EV Station:",
                    labelBottom = if (nearestEv != null) "Available" else "Not found",
                    colorTop = NeonGreen,
                    colorBottom = NeonGreen,
                    icon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonGreen
                        )
                    }
                )

                StatusPill(
                    labelTop = "CNG Station:",
                    labelBottom = if (nearestCng != null) "Available" else "Not found",
                    colorTop = DangerRed,
                    colorBottom = if (nearestCng != null) NeonGreen else DangerRed,
                    icon = {
                        Icon(
                            imageVector = if (nearestCng != null)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Close,
                            contentDescription = null,
                            tint = if (nearestCng != null) NeonGreen else DangerRed
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNav(nav: NavController, context: android.content.Context){
    NavigationBar(containerColor = Night800) {

        NavigationBarItem(
            selected = false,
            onClick = {
                nav.navigate(Screen.StationList.route)
            },
            icon = {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Map",
                    tint = NeonBlue
                )
            },
            label = {
                Text("Book")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                // SOS screen later
            },
            icon = {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "SOS",
                    tint = Color.White
                )
            },
            label = {
                Text("SOS")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                val profile = loadVehicleProfile(context)
                if (profile == null) {
                    nav.navigate(Screen.VehicleSetup.route)
                } else {
                    nav.navigate(Screen.Reachability.route)
                }
            },
            icon = {
                Icon(
                    Icons.Default.BatteryChargingFull,
                    contentDescription = "Fuel Need",
                    tint = Color.White
                )
            },
            label = {
                Text("Fuel Need")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                nav.navigate(Screen.Profile.route)
            },
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "You",
                    tint = Color.White
                )
            },
            label = {
                Text("You")
            }
        )
    }
}