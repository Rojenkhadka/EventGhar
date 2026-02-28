@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.eventghar.ui.user

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventghar.data.BookingDataStore
import com.example.eventghar.ui.organizer.Event
import java.io.File

@Composable
fun UserEventDetailScreen(
    event: Event,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: PublicEventViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )

    val myBookings by viewModel.myBookings.collectAsState()
    val isBooked = myBookings.any { it.eventId == event.id }

    // Live remaining tickets — recomposes instantly when any user books
    val allBookings by BookingDataStore.bookingsFlow(context).collectAsState(initial = emptyList())
    val ticketsSoldLive = allBookings.filter { it.eventId == event.id }.sumOf { it.ticketCount }
    val ticketsTotal = event.ticketsTotal ?: 0
    val ticketsRemaining = (ticketsTotal - ticketsSoldLive).coerceAtLeast(0)
    val isSoldOut = ticketsTotal > 0 && ticketsRemaining == 0

    var ticketCount by remember { mutableIntStateOf(1) }
    var showBookingDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val price = event.price.toFloatOrNull() ?: 0f
    val total = price * ticketCount

    // Booking confirmation dialog
    if (showBookingDialog) {
        AlertDialog(
            onDismissRequest = { showBookingDialog = false },
            title = { Text("Confirm Booking", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Date: ${event.date}  ${event.time}", fontSize = 13.sp, color = Color.Gray)
                    Text("Venue: ${event.location}", fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("Number of Tickets", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilledIconButton(
                            onClick = { if (ticketCount > 1) ticketCount-- },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) { Icon(Icons.Default.Remove, contentDescription = "Decrease") }
                        Text(
                            "$ticketCount",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        FilledIconButton(
                            onClick = { if (ticketsRemaining == 0 || ticketCount < ticketsRemaining) ticketCount++ },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            enabled = ticketsRemaining == 0 || ticketCount < ticketsRemaining
                        ) { Icon(Icons.Default.Add, contentDescription = "Increase") }
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Total Amount:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(
                            if (price == 0f) "Free" else "Rs %.2f".format(total),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.bookEvent(event, ticketCount)
                        showBookingDialog = false
                        showSuccessDialog = true
                    },
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Confirm Booking") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showBookingDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Cancel") }
            }
        )
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C))
                    Spacer(Modifier.width(8.dp))
                    Text("Booking Confirmed!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("You've successfully booked $ticketCount ticket(s) for:")
                    Spacer(Modifier.height(4.dp))
                    Text(event.title, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Go to 'My Bookings' tab to view all your bookings.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Done") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Fixed Book Now button at bottom
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    if (isBooked) {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFF388E3C),
                                disabledContentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Already Booked", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else if (isSoldOut) {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFFD32F2F),
                                disabledContentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sold Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = { showBookingDialog = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Book Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!event.coverImageUri.isNullOrEmpty()) {
                    val detailImgModel: Any? = com.example.eventghar.ui.common.resolveImageModel(event.coverImageUri)
                    if (detailImgModel != null) {
                        AsyncImage(
                            model = detailImgModel,
                            contentDescription = "Event Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(64.dp))
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Category chip
                if (event.category.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            event.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // Title
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp)
                Spacer(Modifier.height(16.dp))

                // Info rows
                EventDetailInfoRow(Icons.Default.CalendarToday, "Date & Time", "${event.date}${if (event.time.isNotBlank()) "  •  ${event.time}" else ""}")
                EventDetailInfoRow(Icons.Default.LocationOn, "Venue", event.location)
                EventDetailInfoRow(
                    Icons.Default.ConfirmationNumber,
                    "Ticket Price",
                    if (event.price.isBlank() || event.price == "0") "Free" else "Rs ${event.price}"
                )
                // Live availability — updates immediately when anyone books
                if (ticketsTotal > 0) {
                    val availColor = when {
                        ticketsRemaining == 0 -> Color(0xFFD32F2F)
                        ticketsRemaining <= (ticketsTotal * 0.1f).coerceAtLeast(3f) -> Color(0xFFE65100)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    EventDetailInfoRow(
                        icon = Icons.Default.People,
                        label = "Availability",
                        value = if (ticketsRemaining == 0) "Sold Out" else "$ticketsRemaining of $ticketsTotal tickets remaining",
                        valueColor = availColor
                    )
                }

                // Description
                if (event.description.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Text("About this Event", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        event.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EventDetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = valueColor ?: MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


