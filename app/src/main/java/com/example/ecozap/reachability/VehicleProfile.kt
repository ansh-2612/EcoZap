package com.example.ecozap.reachability

data class VehicleProfile(
    val vehicleName: String = "",
    val vehicleType: String = "EV",   // EV or CNG
    val fullRangeKm: Int = 0,
    val safetyBufferKm: Int = 10
)

