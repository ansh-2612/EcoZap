package com.example.ecozap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PriceCard(
    title: String,
    price: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CardDarker,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Night800),
                contentAlignment = Alignment.Center
            ) { icon() }

            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = NeonCyan, fontSize = 14.sp)
                Text(price, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

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
        border = BorderStroke(
            width = 1.dp,
            color = NeonGreen.copy(alpha = 0.35f)
        ),
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


@Composable
fun StationCard(
    name: String,
    distanceText: String,
    slotsText: String,
    onBook: () -> Unit
) {
    Surface(
        color = CardDark,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Night800),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.ElectricCar, contentDescription = null, tint = NeonCyan) }

            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Row {
                    Text(distanceText, color = TextDim, fontSize = 14.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(slotsText, color = NeonGreen, fontSize = 14.sp)
                }
            }

            OutlinedButton(
                onClick = onBook,
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
            ) {
                Text("Book")
            }
        }
    }
}

@Composable
fun StatusPill(
    labelTop: String,
    labelBottom: String,
    colorTop: Color,
    colorBottom: Color,
    icon: @Composable () -> Unit
) {
    Surface(
        color = CardDarker,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colorTop.copy(alpha = .15f)),
                contentAlignment = Alignment.Center
            ) { icon() }

            Spacer(Modifier.width(10.dp))
            Column {
                Text(labelTop, color = Color.White, fontSize = 13.sp)
                Text(labelBottom, color = colorBottom, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

