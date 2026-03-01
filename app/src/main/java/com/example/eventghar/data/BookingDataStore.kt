package com.example.eventghar.data

import com.example.eventghar.ui.user.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

    private fun currentUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     * Real-time Flow of bookings for the current authenticated user only.
     * This matches Firestore rules: allow read if userId == request.auth.uid
     */
    fun bookingsFlow(): Flow<List<Booking>> = callbackFlow {
        val uid = currentUid()
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener: ListenerRegistration = bookingsCol
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList<Booking>())
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
     * Real-time Flow of ALL bookings (for organizer analytics / ticket counting).
     * Firestore rule: allow read if request.auth != null
     */
    fun allBookingsFlow(): Flow<List<Booking>> = callbackFlow {
        val listener: ListenerRegistration = bookingsCol
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList<Booking>())
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
     * Flow of ALL bookings for a specific event (used by organizer for ticket count).
     */
    fun bookingsForEventFlow(eventId: String): Flow<List<Booking>> = callbackFlow {
        val listener: ListenerRegistration = bookingsCol
            .whereEqualTo("eventId", eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList<Booking>())
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
     * Add a booking document and increment ticketsSold on the event.
     * Writes are done separately (no transaction) to avoid cross-collection permission issues.
     */
    suspend fun addBooking(booking: Booking) {
        val uid = currentUid()
        if (uid.isBlank()) throw IllegalStateException("User not authenticated")

        // Generate doc ID if not provided
        val docId = if (booking.id.isBlank()) bookingsCol.document().id else booking.id
        val bookingWithId = booking.copy(id = docId, userId = uid)

        // 1. Write the booking document
        bookingsCol.document(docId).set(bookingWithId).await()

        // 2. Increment ticketsSold on the event atomically
        if (booking.eventId.isNotBlank()) {
            eventsCol.document(booking.eventId)
                .update("ticketsSold", FieldValue.increment(booking.ticketCount.toLong()))
                .await()
        }
    }

    suspend fun deleteBooking(bookingId: String) {
        bookingsCol.document(bookingId).delete().await()
    }

    suspend fun deleteBookingsForEvent(eventId: String) {
        val snapshot = bookingsCol
            .whereEqualTo("eventId", eventId)
            .get()
            .await()
        val batch = db.batch()
        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        batch.commit().await()
    }

    // Legacy overloads — context parameter ignored for compile compatibility
    @Suppress("UNUSED_PARAMETER")
    fun bookingsFlow(context: Any): Flow<List<Booking>> = bookingsFlow()

    @Suppress("UNUSED_PARAMETER")
    suspend fun addBooking(context: Any, booking: Booking) = addBooking(booking)

    @Suppress("UNUSED_PARAMETER")
    suspend fun saveBookings(context: Any, bookings: List<Booking>) {
        bookings.forEach { booking -> addBooking(booking) }
    }
}
