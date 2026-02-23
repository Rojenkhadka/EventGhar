package com.example.eventghar.ui.organizer

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class EventViewModel : ViewModel() {
    private var nextId = 1
    var events = mutableStateListOf<Event>()
        private set

    fun addEvent(event: Event) {
        events.add(event.copy(id = nextId++))
    }

    fun updateEvent(updated: Event) {
        val index = events.indexOfFirst { it.id == updated.id }
        if (index != -1) events[index] = updated
    }

    fun deleteEvent(eventId: Int) {
        events.removeAll { it.id == eventId }
    }
}

