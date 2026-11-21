package com.example.ecozap

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.ecozap.booking.*
import com.example.ecozap.ui.navigation.Screen
import java.util.UUID
import kotlin.math.abs

@Composable
fun StationDetailsScreen(navController: NavController, stationId: Int?, api: EcoZapApi) {
    val context = LocalContext.current
    val station = remember(stationId) { loadStationsFromAssets(context).find { it.id == stationId } }
    BookingPage(station?.name ?: "Station details", { navController.popBackStack() }) {
        if (station == null) { Text("Station not found"); return@BookingPage }
        var day by rememberSaveable { mutableStateOf(todayInIndia()) }
        var revision by remember { mutableIntStateOf(0) }
        var notice by rememberSaveable(stationId, day, api.signedIn) { mutableStateOf<String?>(null) }
        val load = remember(day, revision) { RequestState() }
        var live by remember(day, revision) { mutableStateOf<ServerStation?>(null) }
        var slots by remember(day, revision) { mutableStateOf(emptyList<LiveSlot>()) }
        LaunchedEffect(day, revision) {
            load.run {
                val matches = api.stations().filter { s ->
                    s.type.equals(station.type, true) && s.name?.trim().equals(station.name.trim(), true) &&
                        s.latitude?.let { abs(it - station.lat) < 0.001 } == true &&
                        s.longitude?.let { abs(it - station.lng) < 0.001 } == true
                }
                val server = matches.singleOrNull() ?: matches.singleOrNull { it.id == "delhi-${station.id}" }
                if (server != null) slots = api.slots(server.id, day)
                live = server
            }
        }
        Text("${station.type} station")
        var directionError by remember { mutableStateOf(false) }
        OutlinedButton(onClick = {
            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${station.lat},${station.lng}")
            try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)); directionError = false }
            catch (_: android.content.ActivityNotFoundException) { directionError = true }
        }) { Text("Directions") }
        if (directionError) Text("No maps app or browser is available.")
        BookingDate(day) { day = it }
        TextButton(enabled = !load.busy, onClick = { revision++ }) { Text("Refresh availability") }
        RequestStatus(load)
        notice?.let { Text(it) }
        val serverId = live?.id
        if (serverId == null) {
            Text("Directory hours · ${station.slots.joinToString { it.time }}")
            if (!load.busy) Text("Live booking is unavailable for this station.")
        } else {
            key(serverId, day, api.session) {
                ReserveSlot(api, serverId, day, slots, { navController.navigate(Screen.Profile.route) }) { message ->
                    notice = message
                    revision++
                }
            }
        }
    }
}

@Composable
private fun ReserveSlot(api: EcoZapApi, station: String, day: String, slots: List<LiveSlot>, account: () -> Unit, refresh: (String?) -> Unit) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val slot = slots.find { it.id == selected && it.available }
    slots.forEach { s ->
        val label = if (s.scheduled) s.time.orEmpty() else "Bay ${s.slotNumber ?: ""} · now"
        FilterChip(selected = s.id == selected, enabled = s.available, onClick = { selected = s.id },
            label = { Text("$label · ${if (s.available) "Available" else "Unavailable"}") })
    }
    if (slots.none { it.available }) Text("No slots are available for this date.")
    if (!api.signedIn) { Button(onClick = account) { Text("Sign in to book") }; return }
    var vehicles by remember { mutableStateOf(emptyList<Vehicle>()) }
    val loading = remember { RequestState() }
    LaunchedEffect(Unit) { loading.run { vehicles = api.vehicles() } }
    RequestStatus(loading)
    var vehicleId by rememberSaveable { mutableStateOf<String?>(null) }
    val compatible = vehicles.filter { it.type == slot?.vehicleType }
    if (slot != null) {
        Text("Choose your vehicle")
        compatible.forEach { v ->
            FilterChip(selected = vehicleId == v.id, onClick = { vehicleId = v.id },
                label = { Text("${v.brand} ${v.model} · ${v.vehicleNumber}") })
        }
    }
    TextButton(onClick = account) { Text("My vehicles and bookings") }
    val vehicle = compatible.find { it.id == vehicleId } ?: return
    var current by rememberSaveable(vehicle.id) { mutableStateOf("") }
    var target by rememberSaveable(vehicle.id) { mutableStateOf("") }
    val ev = vehicle.type == "EV"
    if (ev) {
        OutlinedTextField(current, { current = it }, label = { Text("Current energy (kWh)") }, singleLine = true)
        OutlinedTextField(target, { target = it }, label = { Text("Target energy (kWh)") }, singleLine = true)
    }
    val start = current.toDoubleOrNull()
    val end = target.toDoubleOrNull()
    val requestId = rememberSaveable(selected, vehicleId, current, target) { UUID.randomUUID().toString() }
    val action = remember { RequestState() }
    val scope = rememberCoroutineScope()
    RequestStatus(action)
    Button(enabled = !action.busy && slot != null &&
            (!ev || (start != null && end != null && start >= 0 && end > start && end <= (vehicle.batteryCapacity ?: 0.0))),
        onClick = {
            val id = slot?.id ?: return@Button
            action.launchIn(scope) {
                val b = try { api.book(BookingInput(vehicle.id, station, id, day, if (ev) start else null,
                    if (ev) end else null, requestId)) } catch (e: ApiException) {
                    if (e.status == 409) { refresh(e.message); return@launchIn }
                    throw e
                }
                refresh("${b.status} · ${displayTime(b.startTime)} IST · Booking ${b.bookingId}")
            }
        }) { Text("Confirm booking") }
    if (action.error != null) TextButton(onClick = { refresh(null) }) { Text("Reload slots") }
}
