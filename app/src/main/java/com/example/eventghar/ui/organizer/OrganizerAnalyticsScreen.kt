package com.example.eventghar.ui.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventghar.ui.user.Booking
import java.io.File

@Composable
fun OrganizerAnalyticsScreen(events: List<Event>, allBookings: List<Booking> = emptyList()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overall", "This Month")

    val publishedEvents = events.filter { it.status == "published" }
    val myEventIds = publishedEvents.map { it.id }.toSet()

    // Only bookings for this organizer's events
    val myBookings = allBookings.filter { it.eventId in myEventIds }

    // Current month filter (MM/yyyy)
    val currentMonth = java.util.Calendar.getInstance().let {
        "%02d/%d".format(it.get(java.util.Calendar.MONTH) + 1, it.get(java.util.Calendar.YEAR))
    }

    val filteredBookings = when (selectedTab) {
        1 -> myBookings.filter { booking ->
            // match booking's event date to current month
            val event = publishedEvents.find { it.id == booking.eventId }
            val parts = event?.date?.split("/")
            if (parts != null && parts.size >= 3) {
                val mm = parts[0].padStart(2, '0')
                val yyyy = parts[2]
                "$mm/$yyyy" == currentMonth
            } else false
        }
        else -> myBookings
    }

    val filteredEvents = when (selectedTab) {
        1 -> publishedEvents.filter { event ->
            val parts = event.date.split("/")
            if (parts.size >= 3) {
                val mm = parts[0].padStart(2, '0')
                val yyyy = parts[2]
                "$mm/$yyyy" == currentMonth
            } else false
        }
        else -> publishedEvents
    }

    // Revenue = sum of all booking totalAmounts for this organizer's events
    val totalRevenue = filteredBookings.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 }

    // Tickets sold = sum of ticketCount in all bookings
    val totalTicketsSold = filteredBookings.sumOf { it.ticketCount }

    // Top events by tickets sold (from bookings)
    val ticketsByEvent = filteredBookings.groupBy { it.eventId }
        .mapValues { (_, bList) -> bList.sumOf { it.ticketCount } }
    val revenueByEvent = filteredBookings.groupBy { it.eventId }
        .mapValues { (_, bList) -> bList.sumOf { it.totalAmount.toDoubleOrNull() ?: 0.0 } }

    val topEvents = filteredEvents.sortedByDescending { ticketsByEvent[it.id] ?: 0 }.take(5)

    // Chart: tickets sold per event (last 7 filtered events)
    val chartEvents = filteredEvents.takeLast(7)
    val maxSold = chartEvents.maxOfOrNull { ticketsByEvent[it.id] ?: 0 }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Notifications, contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Tab Pill ──────────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
        ) {
            Row(Modifier.fillMaxWidth().padding(4.dp)) {
                tabTitles.forEachIndexed { i, title ->
                    val sel = selectedTab == i
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) MaterialTheme.colorScheme.surface else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { selectedTab = i }, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                title,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Summary Cards ─────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsSummaryCard(
                icon = Icons.Default.Payments,
                iconTint = Color(0xFF1976D2),
                iconBg = Color(0xFFE3F2FD),
                title = "TOTAL REVENUE",
                value = "Rs %.0f".format(totalRevenue),
                badge = "+${filteredBookings.size} orders",
                badgeColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            AnalyticsSummaryCard(
                icon = Icons.Default.ConfirmationNumber,
                iconTint = Color(0xFF1976D2),
                iconBg = Color(0xFFE3F2FD),
                title = "TICKETS SOLD",
                value = "$totalTicketsSold",
                badge = if (totalTicketsSold > 0) "$totalTicketsSold sold" else "0 sold",
                badgeColor = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Registrations Bar Chart ────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Daily Registrations", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            "Last ${if (chartEvents.isEmpty()) 7 else chartEvents.size} Events ▾",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (chartEvents.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No data yet", color = Color.Gray)
                    }
                } else {
                    // Bar chart
                    Row(
                        Modifier.fillMaxWidth().height(100.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        chartEvents.forEach { event ->
                            val sold = ticketsByEvent[event.id] ?: 0
                            val frac = sold.toFloat() / maxSold
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$sold", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height((frac * 72).dp.coerceAtLeast(4.dp))
                                        .background(Color(0xFF1976D2),
                                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        chartEvents.forEach { event ->
                            Text(event.title.take(3), fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Top Performing Events ──────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Top Performing Events", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.weight(1f))
            Text("View All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(12.dp))

        if (topEvents.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, contentDescription = null,
                        modifier = Modifier.size(56.dp), tint = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("No published events yet", color = Color.Gray)
                }
            }
        } else {
            Column(Modifier.padding(horizontal = 16.dp)) {
                topEvents.forEach { event ->
                    TopEventCard(
                        event = event,
                        revenue = revenueByEvent[event.id] ?: 0.0,
                        ticketsSold = ticketsByEvent[event.id] ?: 0
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AnalyticsSummaryCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    value: String,
    badge: String,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(50), color = badgeColor.copy(alpha = 0.12f)) {
                    Text(badge, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun TopEventCard(event: Event, revenue: Double = 0.0, ticketsSold: Int = 0) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val coverUri = event.coverImageUri
                if (!coverUri.isNullOrEmpty()) {
                    val analyticsImgModel: Any? = com.example.eventghar.ui.common.resolveImageModel(coverUri)
                    if (analyticsImgModel != null) {
                        AsyncImage(model = analyticsImgModel, contentDescription = event.title,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFF1565C0)), contentAlignment = Alignment.Center) {
                        Text(event.title.take(2).uppercase().ifEmpty { "EV" },
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(3.dp))
                Text("${event.date} • ${event.location}", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("Rs %.0f".format(revenue), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(2.dp))
                Text("$ticketsSold sold", color = Color(0xFF4CAF50),
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}