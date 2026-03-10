package com.example.ecozap.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecozap.Station
import com.example.ecozap.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/* ---------------- PRICE CARD ---------------- */
@Composable
fun PriceCard(
    title: String,
    price: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Night800)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()
            Text(title, color = TextDim, fontSize = 14.sp)
            Text(price, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/* ---------------- ACTION CARD ---------------- */
@Composable
fun ActionCard(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(width = 1.dp, color = NeonGreen.copy(alpha = 0.35f)),
        colors = CardDefaults.outlinedCardColors(containerColor = Night800),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark),
                contentAlignment = Alignment.Center
            ) { icon() }

            Spacer(Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ---------------- STATION CARD ---------------- */
@Composable
fun StationCard(
    name: String,
    distanceText: String,
    slotsText: String,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Night800),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("$distanceText - $slotsText", color = TextDim, fontSize = 14.sp)
            }
            Button(onClick = onBook, colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)) {
                Text("Book", color = Color.Black)
            }
        }
    }
}

/* ---------------- STATUS PILL ---------------- */
@Composable
fun StatusPill(
    labelTop: String,
    labelBottom: String,
    colorTop: Color,
    colorBottom: Color,
    icon: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = Night800)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(8.dp))
            Column {
                Text(labelTop, color = colorTop, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(labelBottom, color = colorBottom, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ---------------- MAP PREVIEW ---------------- */
@Composable
fun MapPreview(
    nearest: Station?,
    userLat: Double?,
    userLng: Double?,
    hasLocation: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (nearest == null) {
            Text("No station selected", color = Color.White)
            return
        }

        val target = LatLng(nearest.lat, nearest.lng)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(target, 14f)
        }

        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocation),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            // Nearest station marker
            Marker(
                state = MarkerState(position = LatLng(nearest.lat, nearest.lng)),
                title = nearest.name,
                snippet = nearest.type
            )

            // User marker
            if (userLat != null && userLng != null) {
                Marker(
                    state = MarkerState(position = LatLng(userLat, userLng)),
                    title = "You"
                )
            }
        }
    }
}
