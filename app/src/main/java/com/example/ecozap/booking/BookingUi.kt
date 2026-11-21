package com.example.ecozap.booking

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class RequestState {
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    suspend fun run(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "The request failed. Please try again."
        } finally {
            busy = false
        }
    }

    fun launchIn(scope: CoroutineScope, block: suspend () -> Unit) {
        if (!busy) scope.launch { run(block) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookingPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
internal fun RequestStatus(state: RequestState) {
    if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

private val stationZone: TimeZone get() = TimeZone.getTimeZone("Asia/Kolkata")
internal fun todayInIndia(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = stationZone }.format(Date())
internal fun displayTime(value: String?): String = value?.take(16)?.replace('T', ' ') ?: "Time unavailable"

@Composable
internal fun BookingDate(day: String, onChange: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        val fields = day.split("-").map { it.toInt() }
        val today = todayInIndia().split("-").map { it.toInt() }
        val calendar = Calendar.getInstance().apply { clear(); set(today[0], today[1] - 1, today[2]) }
        DatePickerDialog(context, { _, y, m, d ->
            onChange(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d))
        }, fields[0], fields[1] - 1, fields[2]).apply { datePicker.minDate = calendar.timeInMillis }.show()
    }) { Text("Booking date · $day (IST)") }
}
