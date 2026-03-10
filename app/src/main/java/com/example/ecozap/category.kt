package com.example.ecozap

import com.google.gson.annotations.SerializedName

data class Slot(
    val time: String,
    val available: Boolean
)

data class Station(
    val id: Int,
    val name: String,

    @SerializedName("latitude")
    val lat: Double,

    @SerializedName("longitude")
    val lng: Double,

    val type: String = "EV",
    val slots: List<Slot> = emptyList()
)