package com.example.ecozap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ecozap.ui.components.MapPreview

import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * MapWithLocation:
 * - requests ACCESS_FINE_LOCATION at runtime
 * - calls getCurrentLocationOnce(fused) (your util)
 * - finds nearest station using findNearestStation()
 * - passes results to MapPreview()
 *
 * usage: MapWithLocation(stations = stationsList, modifier = Modifier.fillMaxSize())
 */
@Composable
fun MapWithLocation(
    stations: List<Station>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // permission state
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        hasPermission = granted
    }

    // kick off permission request once if not granted
    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // user location and nearest station state
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var nearestPair by remember { mutableStateOf<Pair<Station, Float>?>(null) }
    var loading by remember { mutableStateOf(false) }

    // when permission granted, fetch location once and compute nearest station
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            loading = true
            val loc = withContext(Dispatchers.IO) { getCurrentLocationOnce(fusedClient) }
            loc?.let {
                userLocation = it
                nearestPair = findNearestStation(it.latitude, it.longitude, stations)
            }
            loading = false
        }
    }

    // UI states:
    Box(modifier = modifier) {
        when {
            !hasPermission -> {
                // show message and button to request again
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Location permission required to show nearest station", modifier = Modifier.padding(bottom = 8.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                        Text("Grant location permission")
                    }
                }
            }
            loading -> {
                // loading indicator
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                // show the real map inside your MapPreview card
                // MapPreview expects nearest: Station? userLat userLng hasLocation
                MapPreview(
                    nearest = nearestPair?.first,
                    userLat = userLocation?.latitude,
                    userLng = userLocation?.longitude,
                    hasLocation = hasPermission,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}