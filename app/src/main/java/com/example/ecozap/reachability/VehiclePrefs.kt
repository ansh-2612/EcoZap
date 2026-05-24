package com.example.ecozap.reachability

import android.content.Context

private const val PREF_NAME = "vehicle_prefs"
private const val KEY_NAME = "vehicle_name"
private const val KEY_TYPE = "vehicle_type"
private const val KEY_RANGE = "vehicle_range"
private const val KEY_BUFFER = "vehicle_buffer"

fun saveVehicleProfile(context: Context, profile: VehicleProfile) {
    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    prefs.edit()
        .putString(KEY_NAME, profile.vehicleName)
        .putString(KEY_TYPE, profile.vehicleType)
        .putInt(KEY_RANGE, profile.fullRangeKm)
        .putInt(KEY_BUFFER, profile.safetyBufferKm)
        .apply()
}

fun loadVehicleProfile(context: Context): VehicleProfile? {
    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    val name = prefs.getString(KEY_NAME, "") ?: ""
    val type = prefs.getString(KEY_TYPE, "EV") ?: "EV"
    val range = prefs.getInt(KEY_RANGE, 0)
    val buffer = prefs.getInt(KEY_BUFFER, 10)

    if (name.isBlank() || range <= 0) return null

    return VehicleProfile(
        vehicleName = name,
        vehicleType = type,
        fullRangeKm = range,
        safetyBufferKm = buffer
    )
}
