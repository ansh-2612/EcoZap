package com.example.ecozap

import android.Manifest
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ecozap.ui.navigation.Screen
import com.example.ecozap.ui.theme.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CngStationScreen(
    navController: NavController,
    userLat: Double? = null,
    userLng: Double? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

    val stations = remember { loadStationsFromAssets(context, "delhi_stations.json") }

    var selectedType by remember { mutableStateOf("ALL") }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var loadingLocation by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                coroutineScope.launch {
                    loadingLocation = true
                    userLocation = getCurrentLocationOnce(fused)
                    loadingLocation = false
                }
            }
        }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val filteredStations = remember(stations, selectedType) {
        when (selectedType) {
            "EV" -> stations.filter { it.type.equals("EV", true) }
            "CNG" -> stations.filter { it.type.equals("CNG", true) }
            else -> stations
        }
    }

    val sortedStations = remember(filteredStations, userLocation) {
        val loc = userLocation
        if (loc == null) {
            filteredStations
        } else {
            filteredStations.map { station ->
                val result = FloatArray(1)
                Location.distanceBetween(
                    loc.latitude,
                    loc.longitude,
                    station.lat,
                    station.lng,
                    result
                )
                station to result[0]
            }.sortedBy { it.second }
                .map { it.first }
        }
    }

    Scaffold(
        containerColor = Night900,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nearby Stations",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Night800
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Night900)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChipCard(
                    title = "All",
                    selected = selectedType == "ALL",
                    onClick = { selectedType = "ALL" },
                    modifier = Modifier.weight(1f)
                )
                FilterChipCard(
                    title = "EV",
                    selected = selectedType == "EV",
                    onClick = { selectedType = "EV" },
                    modifier = Modifier.weight(1f)
                )
                FilterChipCard(
                    title = "CNG",
                    selected = selectedType == "CNG",
                    onClick = { selectedType = "CNG" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loadingLocation) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonGreen)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedStations) { station ->
                        val distanceText = remember(userLocation, station) {
                            val loc = userLocation
                            if (loc == null) {
                                "N/A"
                            } else {
                                val result = FloatArray(1)
                                Location.distanceBetween(
                                    loc.latitude,
                                    loc.longitude,
                                    station.lat,
                                    station.lng,
                                    result
                                )
                                String.format("%.1f km", result[0] / 1000f)
                            }
                        }

                        val availableCount = station.slots.count { it.available }
                        val slotText = if (availableCount > 0) {
                            "$availableCount slots available"
                        } else {
                            "Fully booked"
                        }

                        StationListCard(
                            station = station,
                            distanceText = distanceText,
                            slotText = slotText,
                            onView = {
                                navController.navigate(Screen.StationDetails.createRoute(station.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) NeonGreen.copy(alpha = 0.16f) else Night800,
            contentColor = if (selected) NeonGreen else Color.White
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (selected) NeonGreen else CardDark
            )
        )
    ) {
        Text(title, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StationListCard(
    station: Station,
    distanceText: String,
    slotText: String,
    onView: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Night800),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (station.type.equals("CNG", true))
                            Icons.Default.LocalGasStation
                        else
                            Icons.Default.ElectricCar,
                        contentDescription = null,
                        tint = if (station.type.equals("CNG", true)) NeonCyan else NeonGreen
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (station.type.equals("CNG", true)) "CNG Filling Station" else "EV Station",
                        color = TextDim
                    )
                }

                Button(
                    onClick = onView,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("View", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$distanceText • $slotText",
                color = TextDim
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (station.slots.count { it.available } == 0) {
                Text(
                    text = "Fully booked",
                    color = DangerRed,
                    fontWeight = FontWeight.Medium
                )
            } else if (station.slots.count { it.available } <= 1) {
                Text(
                    text = "Few slots left",
                    color = DangerRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}