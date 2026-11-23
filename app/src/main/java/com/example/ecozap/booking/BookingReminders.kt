package com.example.ecozap.booking

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** One alarm per booking. Android may defer inexact alarms while the device is idle. */
internal object BookingReminders {
    private const val PREFIX = "reminder:"
    private const val LEAD = 30 * 60_000L
    private fun prefs(c: Context) = c.getSharedPreferences("ecozap_login", Context.MODE_PRIVATE)
    private fun alarm(c: Context, id: String): PendingIntent {
        val intent = Intent(c, BookingReminderReceiver::class.java).apply {
            data = Uri.Builder().scheme("ecozap").authority("reminder").appendPath(id).build()
        }
        return PendingIntent.getBroadcast(c, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun schedule(c: Context, b: Reservation, station: String) {
        if (b.status != "CONFIRMED") { cancel(c, b.bookingId); return }
        val time = b.startTime?.take(16) ?: return
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            isLenient = false
        }
        val start = runCatching { parser.parse(time)?.time }.getOrNull() ?: return
        if (start - LEAD <= System.currentTimeMillis()) return
        val body = "$station · ${displayTime(b.startTime)} IST · Booking ${b.bookingId}"
        prefs(c).edit().putString(PREFIX + b.bookingId,
            JSONObject().put("start", start).put("body", body).toString()).apply()
        arm(c, b.bookingId, start - LEAD)
    }

    private fun arm(c: Context, id: String, at: Long) {
        c.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, alarm(c, id))
    }

    fun cancel(c: Context, id: String) {
        prefs(c).edit().remove(PREFIX + id).apply()
        c.getSystemService(AlarmManager::class.java).cancel(alarm(c, id))
        c.getSystemService(NotificationManager::class.java).cancel(id, 2)
    }

    private fun ids(c: Context) = prefs(c).all.keys.filter { it.startsWith(PREFIX) }.map { it.removePrefix(PREFIX) }
    fun clear(c: Context) = ids(c).forEach { cancel(c, it) }

    fun restore(c: Context) = ids(c).forEach { handle(c, it, true) }
    fun deliver(c: Context, id: String) = handle(c, id, false)

    private fun handle(c: Context, id: String, restore: Boolean) {
        val record = prefs(c).getString(PREFIX + id, null) ?: return
        val reminder = JSONObject(record)
        val now = System.currentTimeMillis()
        val start = reminder.getLong("start")
        when {
            now >= start || !prefs(c).contains("token") -> cancel(c, id)
            restore || now < start - LEAD -> arm(c, id, maxOf(now, start - LEAD))
            else -> {
                prefs(c).edit().remove(PREFIX + id).apply()
                postBookingNotification(c, id, reminder.getString("body"), "Upcoming booking", 2)
            }
        }
    }
}

class BookingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED)
            BookingReminders.restore(context)
        else intent.data?.lastPathSegment?.let { BookingReminders.deliver(context, it) }
    }
}
