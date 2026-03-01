package com.example.eventghar

import com.example.eventghar.ui.organizer.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EventUnitTests {

    @Test
    fun event_defaultValues_areCorrect() {
        val event = Event()
        assertEquals("", event.title)
        assertEquals("", event.category)
        assertEquals(0, event.ticketsSold)
        assertEquals("published", event.status)
    }

    @Test
    fun event_copy_worksCorrectly() {
        val original = Event(title = "Original Title", ticketsTotal = 100)
        val copied = original.copy(title = "New Title")

        assertEquals("New Title", copied.title)
        assertEquals(100, copied.ticketsTotal)
        assertEquals(original.id, copied.id)
    }

    @Test
    fun event_uniqueIds_onCreation() {
        val event1 = Event()
        val event2 = Event()
        assertNotEquals(event1.id, event2.id)
    }
}
