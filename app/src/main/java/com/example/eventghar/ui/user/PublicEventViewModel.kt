package com.example.eventghar.ui.user

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventghar.data.BookingDataStore
import com.example.eventghar.data.EventDataStore
import com.example.eventghar.ui.organizer.Event
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class PublicEventViewModel(application: Application) : AndroidViewModel(application) {

    private val _publishedEvents = MutableStateFlow<List<Event>>(emptyList())
    val publishedEvents: StateFlow<List<Event>> = _publishedEvents.asStateFlow()

    private val _myBookings = MutableStateFlow<List<Booking>>(emptyList())
    val myBookings: StateFlow<List<Booking>> = _myBookings.asStateFlow()

    private val _bookingLoading = MutableStateFlow(false)
    val bookingLoading: StateFlow<Boolean> = _bookingLoading.asStateFlow()

    private val _bookingStatus = MutableStateFlow<Boolean?>(null)
    val bookingStatus: StateFlow<Boolean?> = _bookingStatus.asStateFlow()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        viewModelScope.launch {
            combine(
                EventDataStore.eventsFlow(),
                BookingDataStore.bookingsFlow()
            ) { allEvents: List<Event>, allBookings: List<Booking> ->
                val uid = currentUserId
                // Only published events visible on home/events tab
                val validPublished = allEvents.filter { it.status == "published" }
                // Valid event IDs set — any event that still exists (published or draft)
                val existingEventIds = allEvents.map { it.id }.toSet()
                // Only show bookings whose event still exists in Firestore
                val myFilteredBookings = allBookings.filter {
                    it.userId == uid && it.eventId in existingEventIds
                }
                Pair(validPublished, myFilteredBookings)
            }.collect { pair ->
                _publishedEvents.value = pair.first
                _myBookings.value = pair.second
            }
        }
    }

    fun resetBookingStatus() {
        _bookingStatus.value = null
        _bookingLoading.value = false
    }

    fun bookEvent(event: Event, ticketCount: Int) {
        val uid = currentUserId
        if (uid.isBlank()) {
            Toast.makeText(getApplication(), "Please log in to book tickets", Toast.LENGTH_SHORT).show()
            return
        }

        _bookingLoading.value = true
        _bookingStatus.value = null

        val priceVal = event.price.replace("Rs", "").replace(",", "").trim().toFloatOrNull() ?: 0f
        val total = priceVal * ticketCount
        val booking = Booking(
            eventId       = event.id,
            eventTitle    = event.title,
            eventDate     = event.date,
            eventLocation = event.location,
            eventPrice    = event.price,
            coverImageUri = event.coverImageUri ?: "",
            userId        = uid,
            ticketCount   = ticketCount,
            totalAmount   = String.format(Locale.getDefault(), "%.2f", total)
        )

        viewModelScope.launch {
            try {
                // withContext(NonCancellable) ensures the write finishes even if user leaves screen
                withContext(NonCancellable) {
                    BookingDataStore.addBooking(booking)
                }
                _bookingStatus.value = true
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                Log.e("PublicEventViewModel", "Booking failed", e)
                _bookingStatus.value = false
                Toast.makeText(getApplication(), "Booking failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                _bookingLoading.value = false
            }
        }
    }

    fun isEventBooked(eventId: String): Boolean =
        _myBookings.value.any { it.eventId == eventId }
}
