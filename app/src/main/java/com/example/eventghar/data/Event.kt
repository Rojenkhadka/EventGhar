package com.example.eventghar.data

import java.util.Date

data class Event(
    val name: String,
    val date: Date,
    val description: String,
    val location: String,
    val price: String,
    val image: Int, // Using a drawable resource for the image
    val isTrending: Boolean = false
)