package com.example.ecozap.booking

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ecozap.BuildConfig
import com.example.ecozap.Station
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class ServerStation(val id: String, val name: String?, val type: String?, val latitude: Double?, val longitude: Double?)

data class LiveSlot(val id: String, val slotNumber: Int?, val time: String?, val vehicleType: String,
                    val chargerPower: Double?, val estimatedServiceTimeMinutes: Int?, val scheduled: Boolean,
                    val available: Boolean)
data class Vehicle(val id: String, val vehicleNumber: String, val brand: String, val model: String,
                   val type: String, val batteryCapacity: Double?)
data class Specification(val id: String, val brand: String, val model: String, val type: String)
data class Account(val name: String, val email: String)
data class Reservation(val bookingId: String, val vehicleId: String, val stationId: String, val slotId: String,
                       val startTime: String?, val expectedEndTime: String?, val status: String)
data class BookingInput(val vehicleId: String, val stationId: String, val slotId: String, val serviceDate: String,
                        val currentEnergy: Double?, val targetEnergy: Double?, val requestId: String)

class ApiException(val status: Int, message: String) : IOException(message)

class EcoZapApi(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences("ecozap_login", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var token by mutableStateOf(prefs.getString("token", null))
    val signedIn get() = token != null
    // Distinguish two accounts even when both are signed in.
    val session get() = token

    suspend fun register(name: String, email: String, password: String) {
        request("auth/register", "POST", mapOf("name" to name, "email" to email, "password" to password))
    }

    suspend fun login(email: String, password: String) {
        val jwt = request("auth/login", "POST", mapOf("email" to email, "password" to password)).trim()
        if (jwt.split('.').size != 3) throw IOException("The server returned an invalid sign-in response")
        prefs.edit().putString("token", jwt).apply()
        token = jwt
    }

    fun signOut() {
        BookingReminders.clear(appContext)
        token = null
        prefs.edit().remove("token").apply()
    }

    suspend fun stations(): List<ServerStation> = list("stations")
    suspend fun slots(id: String, day: String): List<LiveSlot> = list("stations/${Uri.encode(id)}/availability?date=$day")
    suspend fun profile(): Account = gson.fromJson(request("user/profile", auth = true), Account::class.java)
    suspend fun vehicles(): List<Vehicle> = list("vehicles", true)
    suspend fun models(term: String): List<Specification> = list("vehicle-specifications/search?query=${Uri.encode(term)}")
    suspend fun addVehicle(modelId: String, number: String) {
        request("vehicles", "POST", mapOf("vehicleSpecificationId" to modelId, "vehicleNumber" to number), true)
    }
    suspend fun history(): List<Reservation> = list("bookings", true)
    suspend fun book(input: BookingInput): Reservation =
        gson.fromJson(request("bookings", "POST", input, true), Reservation::class.java)
    suspend fun cancel(id: String): Reservation =
        gson.fromJson(request("bookings/${Uri.encode(id)}/cancel", "POST", auth = true), Reservation::class.java).also {
            if (it.status == "CANCELLED") BookingReminders.cancel(appContext, it.bookingId)
        }

    private suspend inline fun <reified T> list(path: String, auth: Boolean = false): List<T> =
        gson.fromJson(request(path, auth = auth), TypeToken.getParameterized(List::class.java, T::class.java).type)

    private suspend fun request(path: String, method: String = "GET", body: Any? = null, auth: Boolean = false): String {
        val base = BuildConfig.ECOZAP_API_URL.trimEnd('/')
        if (base.isBlank()) throw IOException("A booking server has not been configured for this app")
        val credential = if (auth) token ?: throw ApiException(401, "Sign in to continue") else null
        return try {
            withContext(Dispatchers.IO) {
                val conn = URL("$base/api/$path").openConnection() as HttpURLConnection
                try {
                    conn.requestMethod = method
                    conn.instanceFollowRedirects = false
                    conn.useCaches = false
                    conn.connectTimeout = 8000
                    conn.readTimeout = 12000
                    conn.setRequestProperty("Accept", "application/json, text/plain")
                    if (credential != null) conn.setRequestProperty("Authorization", "Bearer $credential")
                    if (body != null) {
                        conn.doOutput = true
                        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        conn.outputStream.use { it.write(gson.toJson(body).toByteArray(Charsets.UTF_8)) }
                    }
                    val status = conn.responseCode
                    val stream = if (status in 200..299) conn.inputStream else conn.errorStream
                    val data = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                    if (status !in 200..299) throw ApiException(status, errorText(status, data))
                    data
                } finally {
                    conn.disconnect()
                }
            }
        } catch (e: ApiException) {
            if (e.status == 401 && credential != null && credential == token) signOut()
            throw e
        } catch (e: IOException) {
            val message = when (base) {
                "http://localhost:8081", "http://127.0.0.1:8081" ->
                    "Could not reach the booking server. Start EcoZap on your computer on port 8081, keep USB connected, then refresh."
                "http://10.0.2.2:8081" ->
                    "Could not reach the booking server. Check that EcoZap is running on your computer on port 8081, then refresh."
                else -> "Could not reach the booking server. Check the connection and try again."
            }
            throw IOException(message, e)
        }
    }

    private fun errorText(status: Int, data: String): String {
        if (status == 401) return "Your session has expired. Sign in again."
        val text = runCatching { gson.fromJson(data, JsonObject::class.java)?.get("message")?.asString }.getOrNull()
        if (!text.isNullOrBlank()) return text
        if (status in 400..499 && data.isNotBlank() && data.length <= 180 && !data.trimStart().startsWith("<") &&
            !data.trimStart().startsWith("{")) return data
        return when (status) {
            400 -> "Check the details you entered."
            403 -> "This account cannot perform that action."
            409 -> "The slot changed. Refresh the available slots."
            else -> "EcoZap could not complete the request (HTTP $status)."
        }
    }
}
