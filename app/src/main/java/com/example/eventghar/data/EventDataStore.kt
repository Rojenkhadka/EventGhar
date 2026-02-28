package com.example.eventghar.data

import com.example.eventghar.ui.organizer.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore-backed repository for events.
 * Collection: "events"   Document ID = event.id (UUID string)
 */
object EventDataStore {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("events")

    /**
     * Real-time Flow of ALL events.
     */
    fun eventsFlow(): Flow<List<Event>> = callbackFlow {
        val listener: ListenerRegistration = collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        Event(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            category = doc.getString("category") ?: "",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            location = doc.getString("location") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getString("price") ?: "",
                            ticketsSold = (doc.getLong("ticketsSold") ?: 0L).toInt(),
                            ticketsTotal = (doc.getLong("ticketsTotal") ?: 0L).toInt(),
                            coverImageUri = doc.getString("coverImageUri") ?: "",
                            status = doc.getString("status") ?: "published",
                            organizerId = doc.getString("organizerId") ?: ""
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addEvent(event: Event) {
        collection.document(event.id).set(event).await()
    }

    suspend fun updateEvent(event: Event) {
        collection.document(event.id).set(event, SetOptions.merge()).await()
    }

    suspend fun deleteEvent(eventId: String) {
        // Cascade: delete all bookings for this event first
        try { BookingDataStore.deleteBookingsForEvent(eventId) } catch (_: Exception) {}
        collection.document(eventId).delete().await()
    }

    suspend fun getEvent(eventId: String): Event? {
        return try {
            val doc = collection.document(eventId).get().await()
            if (!doc.exists()) return null
            Event(
                id = doc.id,
                title = doc.getString("title") ?: "",
                category = doc.getString("category") ?: "",
                date = doc.getString("date") ?: "",
                time = doc.getString("time") ?: "",
                location = doc.getString("location") ?: "",
                description = doc.getString("description") ?: "",
                price = doc.getString("price") ?: "",
                ticketsSold = (doc.getLong("ticketsSold") ?: 0L).toInt(),
                ticketsTotal = (doc.getLong("ticketsTotal") ?: 0L).toInt(),
                coverImageUri = doc.getString("coverImageUri") ?: "",
                status = doc.getString("status") ?: "published",
                organizerId = doc.getString("organizerId") ?: ""
            )
        } catch (_: Exception) {
            null
        }
    }

    // Legacy overloads — context parameter ignored for compile compatibility
    @Suppress("UNUSED_PARAMETER")
    fun eventsFlow(context: Any): Flow<List<Event>> = eventsFlow()

    @Suppress("UNUSED_PARAMETER")
    suspend fun saveEvents(context: Any, events: List<Event>) {
        val batch = db.batch()
        events.forEach { event ->
            batch.set(collection.document(event.id), event)
        }
        batch.commit().await()
    }
}
