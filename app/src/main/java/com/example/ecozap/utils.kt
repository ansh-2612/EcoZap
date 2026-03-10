package com.example.ecozap

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun loadStationsFromAssets(
    context: Context,
    filename: String = "delhi_stations.json"
): List<Station> {
    return try {
        val json = context.assets.open(filename).bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Station>>() {}.type
        Gson().fromJson<List<Station>>(json, type) ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

@SuppressLint("MissingPermission")
suspend fun getCurrentLocationOnce(fused: FusedLocationProviderClient): Location? =
    suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()

        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc -> cont.resume(loc) }
            .addOnFailureListener { cont.resume(null) }

        cont.invokeOnCancellation { cts.cancel() }
    }

fun findNearestStation(
    userLat: Double,
    userLng: Double,
    stations: List<Station>
): Pair<Station, Float>? {
    if (stations.isEmpty()) return null

    val result = FloatArray(1)
    var minDist = Float.MAX_VALUE
    var nearest = stations.first()

    for (station in stations) {
        Location.distanceBetween(
            userLat,
            userLng,
            station.lat,
            station.lng,
            result
        )

        val dist = result[0]
        if (dist < minDist) {
            minDist = dist
            nearest = station
        }
    }

    return Pair(nearest, minDist)
}