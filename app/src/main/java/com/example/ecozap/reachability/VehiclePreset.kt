package com.example.ecozap.reachability

data class VehiclePreset(
    val name: String,
    val type: String,
    val fullRangeKm: Int
)

val vehiclePresets = listOf(
    VehiclePreset("Tata Nexon EV", "EV", 325),
    VehiclePreset("Tata Tiago EV", "EV", 250),
    VehiclePreset("MG ZS EV", "EV", 420),
    VehiclePreset("Mahindra XUV400 EV", "EV", 375),
    VehiclePreset("Citroen eC3", "EV", 320),

    VehiclePreset("Maruti WagonR CNG", "CNG", 230),
    VehiclePreset("Maruti Alto K10 CNG", "CNG", 210),
    VehiclePreset("Maruti Swift CNG", "CNG", 220),
    VehiclePreset("Hyundai Aura CNG", "CNG", 240),
    VehiclePreset("Tata Tiago CNG", "CNG", 230)
)

