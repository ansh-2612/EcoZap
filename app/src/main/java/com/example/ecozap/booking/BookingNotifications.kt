package com.example.ecozap.booking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.ecozap.MainActivity
import com.example.ecozap.R

private const val CHANNEL = "booking_updates"

@Composable
internal fun rememberBookingNotification(station: String): (Reservation) -> Unit {
    val context = LocalContext.current
    var pendingId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingText by rememberSaveable { mutableStateOf("") }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingId?.let { if (granted) postBookingNotification(context, it, pendingText) }
        pendingId = null
    }
    return { booking ->
        if (booking.status == "CONFIRMED") {
            BookingReminders.schedule(context, booking, station)
            val body = "$station · ${displayTime(booking.startTime)} IST · Booking ${booking.bookingId}"
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                pendingId = booking.bookingId
                pendingText = body
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else postBookingNotification(context, booking.bookingId, body)
        }
    }
}

internal fun postBookingNotification(context: Context, id: String, body: String, title: String = "Booking confirmed", notificationId: Int = 1): Boolean {
    val manager = context.getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= 26) {
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Booking updates", NotificationManager.IMPORTANCE_DEFAULT))
    }
    if (!manager.areNotificationsEnabled() || (Build.VERSION.SDK_INT >= 26 &&
        manager.getNotificationChannel(CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE)) return false
    val open = PendingIntent.getActivity(context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    val notification = NotificationCompat.Builder(context, CHANNEL)
        .setSmallIcon(R.drawable.ic_booking_notification).setContentTitle(title)
        .setContentText(body).setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setContentIntent(open).setAutoCancel(true).setOnlyAlertOnce(true)
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
    return try { manager.notify(id, notificationId, notification); true } catch (_: SecurityException) {
        false
        // Permission may have changed while the booking was being confirmed.
    }
}

@Composable
internal fun NotificationCheck() {
    val context = LocalContext.current
    var sent by rememberSaveable { mutableStateOf<Boolean?>(null) }
    fun sendSample() {
        sent = postBookingNotification(context, "sample", "Booking alerts are enabled on this device.",
            "EcoZap test notification", 3)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) sendSample() else sent = false
    }
    androidx.compose.material3.OutlinedButton(onClick = {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        else sendSample()
    }) { androidx.compose.material3.Text("Test booking notification") }
    if (sent == true) androidx.compose.material3.Text("Swipe down to see the EcoZap test notification.")
    if (sent == false) {
        androidx.compose.material3.Text("Notifications are blocked. Enable EcoZap and its Booking updates channel, then try again.")
        androidx.compose.material3.TextButton(onClick = {
            val settings = if (Build.VERSION.SDK_INT >= 26)
                Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            else Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null))
            context.startActivity(settings)
        }) { androidx.compose.material3.Text("Open notification settings") }
    }
}
