package com.example.eventghar.ui.organizer

import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val category: String = "", // <-- added
    val date: String = "",
    val time: String = "", // <-- added
    val location: String = "",
    val description: String = "",
    val price: String = "", // <-- added
    val ticketsSold: Int = 0,
    val ticketsTotal: Int = 0,
    val coverImageUri: String? = "",
    val status: String = "published", // "published" or "draft"
    val organizerId: String = "" // Firebase Auth UID of the organizer who created this event
)
