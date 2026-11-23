package com.example.ecozap.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ecozap.Station
import kotlinx.coroutines.delay

@Composable
fun AccountScreen(api: EcoZapApi, stations: List<Station>, onBack: () -> Unit) {
    BookingPage("My account", onBack) {
        NotificationCheck()
        key(api.session) {
            if (api.signedIn) SignedInAccount(api, stations) else SignInForm(api)
        }
    }
}

@Composable
private fun SignInForm(api: EcoZapApi) {
    var create by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val state = remember { RequestState() }
    val scope = rememberCoroutineScope()
    Text(if (create) "Create an account" else "Sign in to EcoZap", style = MaterialTheme.typography.headlineSmall)
    if (create) OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(),
        label = { Text("Name") }, enabled = !state.busy, singleLine = true)
    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), enabled = !state.busy, singleLine = true)
    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        enabled = !state.busy, singleLine = true)
    if (create) Text("Use at least 6 characters.")
    if (note.isNotEmpty()) Text(note)
    RequestStatus(state)
    Button(enabled = !state.busy && email.isNotBlank() && password.isNotEmpty() &&
            (!create || (name.isNotBlank() && password.length >= 6)), onClick = {
        state.launchIn(scope) {
            if (create) {
                api.register(name.trim(), email.trim(), password)
                create = false
                note = "Account created. You can sign in with this email."
            }
            api.login(email.trim(), password)
            password = ""
        }
    }) { Text(if (create) "Create account" else "Sign in") }
    TextButton(enabled = !state.busy, onClick = { create = !create; note = "" }) {
        Text(if (create) "I already have an account" else "Create an account")
    }
}

@Composable
private fun SignedInAccount(api: EcoZapApi, stations: List<Station>) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var revision by remember { mutableIntStateOf(0) }
    var profile by remember { mutableStateOf<Account?>(null) }
    var vehicles by remember { mutableStateOf(listOf<Vehicle>()) }
    var bookings by remember { mutableStateOf(listOf<Reservation>()) }
    var loaded by remember { mutableStateOf(false) }
    var editor by rememberSaveable { mutableStateOf(false) }
    var cancelId by remember { mutableStateOf<String?>(null) }
    val load = remember(revision) { RequestState() }
    val action = remember { RequestState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(revision) {
        load.run {
            profile = api.profile()
            vehicles = api.vehicles()
            bookings = api.history()
            bookings.forEach { b ->
                val station = stations.find { "delhi-${it.id}" == b.stationId }
                BookingReminders.schedule(context, b, station?.name ?: "Station ${b.stationId}")
            }
            loaded = true
        }
    }
    profile?.let { Text(it.name, style = MaterialTheme.typography.headlineSmall); Text(it.email) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(enabled = !load.busy && !action.busy, onClick = { revision++ }) { Text("Refresh") }
        TextButton(enabled = !action.busy, onClick = api::signOut) { Text("Sign out") }
    }
    RequestStatus(load)
    if (!loaded) return
    HorizontalDivider()
    Text("My vehicles", style = MaterialTheme.typography.titleLarge)
    vehicles.forEach { Text("${it.brand} ${it.model} · ${it.vehicleNumber}") }
    if (vehicles.isEmpty()) Text("Add your vehicle to reserve a compatible slot.")
    if (editor || vehicles.isEmpty()) {
        VehicleEditor(api) { editor = false; revision++ }
    } else {
        OutlinedButton(onClick = { editor = true }) { Text("Add a vehicle") }
    }
    HorizontalDivider()
    Text("My bookings", style = MaterialTheme.typography.titleLarge)
    if (bookings.isEmpty()) Text("You have no bookings yet.")
    RequestStatus(action)
    bookings.forEach { b ->
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stations.find { "delhi-${it.id}" == b.stationId }?.name ?: "Station ${b.stationId}",
                    style = MaterialTheme.typography.titleMedium)
                Text("${displayTime(b.startTime)} IST · ${b.status.lowercase().replaceFirstChar { it.uppercase() }}")
                Text("Vehicle · ${vehicles.find { it.id == b.vehicleId }?.vehicleNumber ?: b.vehicleId}")
                Text("Booking ${b.bookingId}", style = MaterialTheme.typography.bodySmall)
                if (b.status == "CONFIRMED") TextButton(enabled = !action.busy,
                    onClick = { cancelId = b.bookingId }) { Text("Cancel booking") }
            }
        }
    }
    cancelId?.let { id ->
        AlertDialog(onDismissRequest = { cancelId = null }, title = { Text("Cancel this booking?") },
            text = { Text("The slot will become available to other drivers.") },
            confirmButton = { TextButton(onClick = {
                cancelId = null
                action.launchIn(scope) {
                    try { api.cancel(id) } finally { revision++ }
                }
            }) { Text("Cancel booking") } },
            dismissButton = { TextButton(onClick = { cancelId = null }) { Text("Keep booking") } })
    }
}

@Composable
private fun VehicleEditor(api: EcoZapApi, onAdded: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var chosen by rememberSaveable(query) { mutableStateOf<String?>(null) }
    var models by remember(query) { mutableStateOf(listOf<Specification>()) }
    var reload by remember { mutableIntStateOf(0) }
    val search = remember(query, reload) { RequestState() }
    val save = remember { RequestState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(query, reload) {
        delay(200)
        search.run { models = api.models(query.trim()) }
    }
    OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search by brand or model") },
        singleLine = true)
    RequestStatus(search)
    if (search.error != null) TextButton(onClick = { reload++ }) { Text("Retry search") }
    if (!search.busy && search.error == null && models.isEmpty()) Text("No vehicle models found.")
    models.take(20).forEach { m ->
        FilterChip(selected = chosen == m.id, onClick = { chosen = m.id },
            label = { Text("${m.brand} ${m.model} · ${m.type}") })
    }
    OutlinedTextField(number, { number = it }, Modifier.fillMaxWidth(),
        label = { Text("Vehicle registration number") }, singleLine = true)
    RequestStatus(save)
    Button(enabled = !save.busy && chosen != null && number.isNotBlank(), onClick = {
        val model = chosen ?: return@Button
        save.launchIn(scope) { api.addVehicle(model, number.trim()); number = ""; onAdded() }
    }) { Text("Save vehicle") }
}
