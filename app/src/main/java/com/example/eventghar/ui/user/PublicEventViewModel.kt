package com.example.eventghar.ui.user

import android.app.Application
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

data class Booking(
    val id: String = java.util.UUID.randomUUID().toString(),
    val eventId: String = "",
    val eventTitle: String = "",
    val eventDate: String = "",
    val eventLocation: String = "",
    val eventPrice: String = "",
    val coverImageUri: String = "",
    val userId: String = "",
    val ticketCount: Int = 1,
    val totalAmount: String = ""
)

class PublicEventViewModel(application: Application) : AndroidViewModel(application) {

    // All published events from all organizers
    private val _publishedEvents = MutableStateFlow<List<Event>>(emptyList())
    val publishedEvents: StateFlow<List<Event>> = _publishedEvents.asStateFlow()

    // Bookings for the current user
    private val _myBookings = MutableStateFlow<List<Booking>>(emptyList())
    val myBookings: StateFlow<List<Booking>> = _myBookings.asStateFlow()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        viewModelScope.launch {
            combine(
                EventDataStore.eventsFlow(),
                BookingDataStore.bookingsFlow()
            ) { allEvents: List<Event>, allBookings: List<Booking> ->
                val validPublished = allEvents.filter { event ->
                    event.status == "published" &&
                    event.title.trim().length >= 3 &&
                    event.location.trim().length >= 2 &&
                    event.date.isNotBlank() &&
                    (event.price.isBlank() || event.price.toDoubleOrNull() != null)
                }
                val existingEventIds = allEvents.map { it.id }.toSet()
                val myFilteredBookings = allBookings.filter { booking ->
                    booking.userId == currentUserId &&
                    existingEventIds.contains(booking.eventId)
                }
                Pair(validPublished, myFilteredBookings)
            }.collect { pair ->
                _publishedEvents.value = pair.first
                _myBookings.value = pair.second
            }
        }
    }

    fun bookEvent(event: Event, ticketCount: Int) {
        val price = event.price.toFloatOrNull() ?: 0f
        val total = price * ticketCount
        val booking = Booking(
            eventId       = event.id,
            eventTitle    = event.title,
            eventDate     = event.date,
            eventLocation = event.location,
            eventPrice    = event.price,
            coverImageUri = event.coverImageUri ?: "",
            userId        = currentUserId,
            ticketCount   = ticketCount,
            totalAmount   = String.format(Locale.getDefault(), "%.2f", total)
        )
        viewModelScope.launch {
            BookingDataStore.addBooking(booking)
        }
    }

    fun isEventBooked(eventId: String): Boolean =
        _myBookings.value.any { it.eventId == eventId && it.userId == currentUserId }
}
