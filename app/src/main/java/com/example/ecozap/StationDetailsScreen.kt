package com.example.ecozap

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ecozap.ui.theme.CardDark
import com.example.ecozap.ui.theme.DangerRed
import com.example.ecozap.ui.theme.NeonCyan
import com.example.ecozap.ui.theme.NeonGreen
import com.example.ecozap.ui.theme.Night800
import com.example.ecozap.ui.theme.Night900
import com.example.ecozap.ui.theme.TextDim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailsScreen(
    navController: NavController,
    stationId: Int?
) {
    val context = LocalContext.current
    val stations = remember { loadStationsFromAssets(context, "delhi_stations.json") }
    val station = stations.find { it.id == stationId }

    if (station == null) {
        Scaffold(
            containerColor = Night900,
            topBar = {
                TopAppBar(
                    title = { Text("Station Details", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Night800)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Station not found", color = Color.White)
            }
        }
        return
    }

    val localSlots = remember(station.id) {
        mutableStateListOf<Slot>().apply {
            addAll(station.slots)
        }
    }

    var selectedIndex by remember { mutableIntStateOf(-1) }
    var bookingMessage by remember { mutableStateOf("") }

    // local rule: one user can book only one slot
    var bookedSlotIndex by remember { mutableIntStateOf(-1) }

    // local queue state
    var isInQueue by remember { mutableStateOf(false) }
    var queuePosition by remember { mutableIntStateOf(0) }

    val availableCount = localSlots.count { it.available }
    val allSlotsFull = availableCount == 0

    Scaffold(
        containerColor = Night900,
        topBar = {
            TopAppBar(
                title = { Text("Station Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Night800)
            )
        }
    ) { paddingValues ->
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = if (station.type.equals("CNG", true))
                            Icons.Default.LocalGasStation
                        else
                            Icons.Default.ElectricCar,
                        contentDescription = null,
                        tint = if (station.type.equals("CNG", true)) NeonCyan else NeonGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = station.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Type: ${station.type}",
                        color = TextDim
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Slots available: $availableCount",
                        color = NeonGreen
                    )

                    if (bookedSlotIndex != -1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your booked slot: ${localSlots[bookedSlotIndex].time}",
                            color = NeonCyan
                        )
                    }

                    if (isInQueue) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You are in waiting list • Position #$queuePosition",
                            color = NeonCyan
                        )
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Select Slot",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    localSlots.forEachIndexed { index, slot ->
                        val isSelected = selectedIndex == index
                        val isMyBookedSlot = bookedSlotIndex == index

                        val bgColor = when {
                            isMyBookedSlot -> NeonCyan.copy(alpha = 0.18f)
                            !slot.available -> CardDark
                            isSelected -> NeonGreen.copy(alpha = 0.18f)
                            else -> Color.Transparent
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = bgColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    enabled = slot.available && bookedSlotIndex == -1 && !isInQueue
                                ) {
                                    selectedIndex = index
                                    bookingMessage = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = slot.time,
                                color = if (slot.available || isMyBookedSlot) Color.White else TextDim
                            )

                            Text(
                                text = when {
                                    isMyBookedSlot -> "Your Slot"
                                    !slot.available -> "Booked"
                                    isSelected -> "Selected"
                                    else -> "Available"
                                },
                                color = when {
                                    isMyBookedSlot -> NeonCyan
                                    !slot.available -> DangerRed
                                    else -> NeonGreen
                                }
                            )
                        }

                        if (index != localSlots.lastIndex) {
                            HorizontalDivider(color = CardDark)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    bookingMessage = when {
                        isInQueue -> "You are already in the waiting list"
                        bookedSlotIndex != -1 -> "You can only book one slot"
                        selectedIndex == -1 -> "Please select an available slot"
                        !localSlots[selectedIndex].available -> "This slot is already booked"
                        else -> {
                            val current = localSlots[selectedIndex]
                            localSlots[selectedIndex] = current.copy(available = false)
                            bookedSlotIndex = selectedIndex
                            selectedIndex = -1
                            "Slot booked: ${current.time}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = availableCount > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Book Selected Slot", color = Color.Black)
            }

            if (allSlotsFull) {
                Button(
                    onClick = {
                        bookingMessage = if (bookedSlotIndex != -1) {
                            "You already have a booked slot"
                        } else if (isInQueue) {
                            "You are already in the waiting list"
                        } else {
                            isInQueue = true
                            queuePosition = 3
                            "Joined waiting list at position #$queuePosition"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text(
                        text = if (isInQueue) "Waiting List Joined" else "Join Waiting List",
                        color = Color.Black
                    )
                }
            }

            if (bookingMessage.isNotEmpty()) {
                Text(
                    text = bookingMessage,
                    color = if (
                        bookingMessage.startsWith("Slot booked") ||
                        bookingMessage.startsWith("Joined waiting list")
                    ) NeonGreen else DangerRed
                )
            }

            Button(
                onClick = {
                    val uri = Uri.parse("google.navigation:q=${station.lat},${station.lng}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Navigate", color = Color.Black)
            }
        }
    }
}