package com.example.eventghar.ui.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventghar.data.BookingDataStore
import java.io.File

@Composable
fun OrganizerEventDetailsScreen(
    event: Event,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Live-reactive bookings — recomposes whenever a user books a ticket
    val allBookings by BookingDataStore.bookingsFlow(context).collectAsState(initial = emptyList())
    val ticketsSold = allBookings.filter { it.eventId == event.id }.sumOf { it.ticketCount }
    val ticketsTotal = event.ticketsTotal ?: 0
    val ticketsAvailable = (ticketsTotal - ticketsSold).coerceAtLeast(0)
    val progress = if (ticketsTotal > 0) ticketsSold.toFloat() / ticketsTotal else 0f
    val progressPercent = (progress * 100).toInt()
    val progressColor = when {
        ticketsAvailable == 0 -> Color(0xFFD32F2F)
        ticketsAvailable <= (ticketsTotal * 0.1f).coerceAtLeast(3f) -> Color(0xFFFF6F00)
        else -> Color(0xFF1976D2)
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1976D2)
                    )
                }
                Text(
                    "Event Details",
                    Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Scrollable Content
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Cover Image
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (!event.coverImageUri.isNullOrEmpty()) {
                    val imageModel: Any? = com.example.eventghar.ui.common.resolveImageModel(event.coverImageUri)
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Event Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1976D2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = event.title.take(2).uppercase().ifEmpty { "EV" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 64.sp
                        )
                    }
                }
            }

            // Event Content
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Event Title
                Text(
                    text = event.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (event.status == "published") Color(0xFF4CAF50) else Color(0xFFFFC107),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = if (event.status == "published") "Published" else "Draft",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Date & Time Section
                DetailInfoCard(
                    icon = Icons.Default.CalendarToday,
                    title = "Date & Time",
                    content = event.date,
                    iconTint = Color(0xFF1976D2)
                )

                Spacer(Modifier.height(12.dp))

                // Location Section
                DetailInfoCard(
                    icon = Icons.Default.LocationOn,
                    title = "Venue",
                    content = event.location,
                    iconTint = Color(0xFFE91E63)
                )

                Spacer(Modifier.height(12.dp))

                // Ticket Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFE3F2FD), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Ticket Information",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Registration Progress — live from bookings
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Registrations",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "$ticketsSold / $ticketsTotal",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$progressPercent%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor
                                )
                                // Available tickets badge
                                val availableColor = when {
                                    ticketsAvailable == 0 -> Color(0xFFD32F2F)
                                    ticketsAvailable <= (ticketsTotal * 0.1f).coerceAtLeast(3f) -> Color(0xFFFF6F00)
                                    else -> Color(0xFF388E3C)
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = availableColor.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        if (ticketsAvailable == 0) "Sold Out" else "$ticketsAvailable left",
                                        color = availableColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.height(12.dp))

                        // Price + Revenue row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Price / Ticket", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (event.price.isBlank() || event.price == "0") "Free"
                                    else "Rs ${event.price}",
                                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Revenue", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val revenue = (event.price.toDoubleOrNull() ?: 0.0) * ticketsSold
                                Text(
                                    "Rs %.0f".format(revenue),
                                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                    color = Color(0xFF388E3C)
                                )
                            }
                        }
                    } // end Column inside Card
                } // end Ticket Information Card

                Spacer(Modifier.height(12.dp))

                // Description Section (if available)
                if (event.description.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Description",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                event.description,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DetailInfoCard(
    icon: ImageVector,
    title: String,
    content: String,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(iconTint.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    content,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

