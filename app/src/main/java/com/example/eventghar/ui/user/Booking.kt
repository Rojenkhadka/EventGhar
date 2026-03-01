package com.example.eventghar.ui.user

data class Booking(
    val id: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val eventDate: String = "",
    val eventLocation: String = "",
    val eventPrice: String = "",
    val coverImageUri: String = "",
    val userId: String = "",
    val ticketCount: Int = 1,
    val totalAmount: String = "0.00",
    val bookingDate: String = "",
    val status: String = "confirmed"
)


