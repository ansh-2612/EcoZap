package com.example.ecozap.reachability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ecozap.ui.navigation.Screen
import com.example.ecozap.ui.theme.DangerRed
import com.example.ecozap.ui.theme.NeonGreen
import com.example.ecozap.ui.theme.Night800
import com.example.ecozap.ui.theme.Night900
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import com.example.ecozap.ui.theme.CardDark
import com.example.ecozap.ui.theme.TextDim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSetupScreen(navController: NavController) {
    val context = LocalContext.current
    val savedProfile = loadVehicleProfile(context)

    var vehicleName by remember {
        mutableStateOf(savedProfile?.vehicleName ?: "")
    }

    var vehicleType by remember {
        mutableStateOf(savedProfile?.vehicleType ?: "EV")
    }

    var fullRange by remember {
        mutableStateOf(savedProfile?.fullRangeKm?.toString() ?: "")
    }

    var safetyBuffer by remember {
        mutableStateOf(savedProfile?.safetyBufferKm?.toString() ?: "10")
    }

    var errorText by remember {
        mutableStateOf("")
    }
    var showSuggestions by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Night900,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vehicle Setup",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Night800
                )
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    OutlinedTextField(
                        value = vehicleName,
                        onValueChange = {
                            vehicleName = it
                            errorText = ""
                            showSuggestions = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Vehicle Name")
                        },
                        singleLine = true
                    )

                    val filteredPresets = vehiclePresets.filter {
                        it.name.contains(vehicleName, ignoreCase = true) ||
                                it.type.contains(vehicleName, ignoreCase = true)
                    }

                    if (showSuggestions && vehicleName.isNotBlank() && filteredPresets.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Night800)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                items(filteredPresets) { preset ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                vehicleName = preset.name
                                                vehicleType = preset.type
                                                fullRange = preset.fullRangeKm.toString()
                                                showSuggestions = false
                                                errorText = ""
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = preset.name,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${preset.type} • ${preset.fullRangeKm} km range",
                                            color = TextDim
                                        )
                                    }

                                    HorizontalDivider(color = CardDark)
                                }
                            }
                        }
                    }

                    Text(
                        text = "Vehicle Type",
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                vehicleType = "EV"
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (vehicleType == "EV")
                                    NeonGreen.copy(alpha = 0.18f)
                                else
                                    Night800,
                                contentColor = if (vehicleType == "EV")
                                    NeonGreen
                                else
                                    Color.White
                            )
                        ) {
                            Text("EV")
                        }

                        OutlinedButton(
                            onClick = {
                                vehicleType = "CNG"
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (vehicleType == "CNG")
                                    NeonGreen.copy(alpha = 0.18f)
                                else
                                    Night800,
                                contentColor = if (vehicleType == "CNG")
                                    NeonGreen
                                else
                                    Color.White
                            )
                        ) {
                            Text("CNG")
                        }
                    }

                    OutlinedTextField(
                        value = fullRange,
                        onValueChange = {
                            fullRange = it.filter { ch -> ch.isDigit() }
                            errorText = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Full Range on 100% / Full Tank (km)")
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = safetyBuffer,
                        onValueChange = {
                            safetyBuffer = it.filter { ch -> ch.isDigit() }
                            errorText = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Safety Buffer (km)")
                        },
                        singleLine = true
                    )

                    if (errorText.isNotEmpty()) {
                        Text(
                            text = errorText,
                            color = DangerRed
                        )
                    }

                    Button(
                        onClick = {
                            val rangeValue = fullRange.toIntOrNull()
                            val bufferValue = safetyBuffer.toIntOrNull()

                            when {
                                vehicleName.isBlank() -> {
                                    errorText = "Enter vehicle name"
                                }

                                rangeValue == null || rangeValue <= 0 -> {
                                    errorText = "Enter valid full range"
                                }

                                bufferValue == null || bufferValue < 0 -> {
                                    errorText = "Enter valid safety buffer"
                                }

                                bufferValue >= rangeValue -> {
                                    errorText = "Buffer should be less than full range"
                                }

                                else -> {
                                    saveVehicleProfile(
                                        context = context,
                                        profile = VehicleProfile(
                                            vehicleName = vehicleName.trim(),
                                            vehicleType = vehicleType,
                                            fullRangeKm = rangeValue,
                                            safetyBufferKm = bufferValue
                                        )
                                    )

                                    navController.navigate(Screen.Reachability.route) {
                                        popUpTo(Screen.VehicleSetup.route) {
                                            inclusive = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Save & Continue",
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

