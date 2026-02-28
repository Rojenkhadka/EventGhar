package com.example.eventghar.ui.organizer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventghar.data.EventDataStore
import com.example.eventghar.data.StorageUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        viewModelScope.launch {
            EventDataStore.eventsFlow().collect { allEvents ->
                _events.value = allEvents.filter { it.organizerId == currentUserId }
            }
        }
    }

    fun addEvent(event: Event) {
        val eventWithOrganizer = event.copy(organizerId = currentUserId)
        viewModelScope.launch {
            // Upload cover image to Firebase Storage if it's a local path
            val finalEvent = uploadCoverImageIfNeeded(eventWithOrganizer)
            EventDataStore.addEvent(finalEvent)
        }
    }

    fun updateEvent(updated: Event) {
        val updatedWithOrganizer = updated.copy(organizerId = currentUserId)
        viewModelScope.launch {
            val finalEvent = uploadCoverImageIfNeeded(updatedWithOrganizer)
            EventDataStore.updateEvent(finalEvent)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            // Also delete the cover image from Storage
            val event = EventDataStore.getEvent(eventId)
            if (event != null && StorageUtil.isRemoteUrl(event.coverImageUri)) {
                StorageUtil.deleteImage(event.coverImageUri)
            }
            EventDataStore.deleteEvent(eventId)
        }
    }

    /**
     * If coverImageUri is a local/content URI, upload to Firebase Storage
     * and return the event with the HTTPS download URL.
     * If already an HTTPS URL, return as-is.
     * If upload fails, store empty string so the card shows the placeholder gracefully.
     */
    private suspend fun uploadCoverImageIfNeeded(event: Event): Event {
        val uri = event.coverImageUri
        if (uri.isNullOrBlank() || StorageUtil.isRemoteUrl(uri)) return event
        // Local path or content:// URI — upload to Firebase Storage
        val downloadUrl = StorageUtil.uploadImage(
            getApplication(), "event_images", uri
        )
        return event.copy(coverImageUri = downloadUrl ?: "")
    }
}
