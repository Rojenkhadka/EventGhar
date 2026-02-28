package com.example.eventghar.data

import com.example.eventghar.ui.user.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore-backed repository for bookings.
 * Collection: "bookings"   Document ID = booking.id (UUID string)
 */
object BookingDataStore {

    private val db = FirebaseFirestore.getInstance()
    private val bookingsCol = db.collection("bookings")
    private val eventsCol = db.collection("events")

    /**
     * Real-time Flow of ALL bookings (filters happen in ViewModels / UI).
     */
    fun bookingsFlow(): Flow<List<Booking>> = callbackFlow {
        val listener: ListenerRegistration = bookingsCol
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val bookings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Atomically add a booking AND increment ticketsSold on the event document.
     * Uses a Firestore transaction for cross-document consistency.
     */
    suspend fun addBooking(booking: Booking) {
        val eventRef = eventsCol.document(booking.eventId)
        val bookingRef = bookingsCol.document(booking.id)

        db.runTransaction { transaction ->
            val eventSnapshot = transaction.get(eventRef)
            val currentSold = eventSnapshot.getLong("ticketsSold")?.toInt() ?: 0
            transaction.update(eventRef, "ticketsSold", currentSold + booking.ticketCount)
            transaction.set(bookingRef, booking)
        }.await()
    }

    /**
     * Delete a single booking by ID.
     */
    suspend fun deleteBooking(bookingId: String) {
        bookingsCol.document(bookingId).delete().await()
    }

    /**
     * Cascade-delete ALL bookings that belong to a given event.
     * Called when an organizer deletes an event so users' bookings are cleaned up immediately.
     */
    suspend fun deleteBookingsForEvent(eventId: String) {
        val snapshot = bookingsCol
            .whereEqualTo("eventId", eventId)
            .get()
            .await()
        val batch = db.batch()
        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        batch.commit().await()
    }

    // ── Legacy overloads — context parameter ignored for compile compatibility ──

    @Suppress("UNUSED_PARAMETER")
    fun bookingsFlow(context: Any): Flow<List<Booking>> = bookingsFlow()

    @Suppress("UNUSED_PARAMETER")
    suspend fun addBooking(context: Any, booking: Booking) = addBooking(booking)

    @Suppress("UNUSED_PARAMETER")
    suspend fun saveBookings(context: Any, bookings: List<Booking>) {
        val batch = db.batch()
        bookings.forEach { booking ->
            batch.set(bookingsCol.document(booking.id), booking)
        }
        batch.commit().await()
    }
}
