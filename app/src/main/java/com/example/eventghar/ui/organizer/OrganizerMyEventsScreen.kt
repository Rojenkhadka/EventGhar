package com.example.eventghar.ui.organizer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventghar.data.BookingDataStore
import com.example.eventghar.ui.common.resolveImageModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun OrganizerMyEventsScreen(
    events: List<Event>,
    onManage: (Event) -> Unit,
    onDelete: (String) -> Unit,
    onViewDetails: (Event) -> Unit,
    onPublish: (Event) -> Unit // <-- new callback
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Drafts", "Past")

    val filtered = remember(events, query, selectedTab) {
        val base = if (query.isBlank()) events else events.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.location.contains(query, ignoreCase = true)
        }
        when (selectedTab) {
            0 -> base.filter { it.status == "published" } // Upcoming
            1 -> base.filter { it.status == "draft" } // Drafts
            2 -> base.filter { it.status == "published" } // Past (for now, same as upcoming)
            else -> base
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Events",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            // REMOVED: Search icon from header
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }

        // ── Search Bar ──────────────────────────────────────────────────
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            placeholder = { Text("Search your events...", color = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color(0xFF1976D2)
            )
        )

        // ── Tabs ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = index }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) Color(0xFF1976D2) else Color.Gray,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                    if (selectedTab == index) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier
                                .width(40.dp)
                                .height(3.dp)
                                .background(Color(0xFF1976D2), RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

        // ── Event List ───────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No events yet. Tap + to create your first event.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { event ->
                    MyEventCard(
                        event = event,
                        onManage = { onManage(event) },
                        onDelete = { onDelete(event.id) },
                        onViewDetails = { onViewDetails(event) },
                        onPublish = { onPublish(event) } // <-- pass callback
                    )
                }
            }
        }
    }
}

@Composable
private fun MyEventCard(
    event: Event,
    onManage: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit,
    onPublish: (() -> Unit)? = null // <-- optional for drafts
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Live-reactive: recomposes whenever any booking is added/removed
    val allBookings by BookingDataStore.bookingsFlow(context).collectAsState(initial = emptyList())
    val ticketsSold = allBookings.filter { it.eventId == event.id }.sumOf { it.ticketCount }
    val ticketsTotal = event.ticketsTotal
    val ticketsAvailable = (ticketsTotal - ticketsSold).coerceAtLeast(0)
    val progress = if (ticketsTotal > 0) ticketsSold.toFloat() / ticketsTotal else 0f
    val progressPercent = (progress * 100).toInt()
    val coverImageUri = event.coverImageUri ?: ""

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete \"${event.title}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (coverImageUri.isNotEmpty()) {
                    val imageModel: Any? = resolveImageModel(coverImageUri)
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = event.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(Color(0xFF1565C0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(event.title.take(2).uppercase().ifEmpty { "EV" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    }
                } else {
                    Box(
                        Modifier.fillMaxSize().background(Color(0xFF1565C0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(event.title.take(2).uppercase().ifEmpty { "EV" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }
            } // end thumbnail Box

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title + menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (event.status == "draft" && onPublish != null) {
                                DropdownMenuItem(text = { Text("Publish", color = Color(0xFF43A047)) }, onClick = { showMenu = false; onPublish() })
                            }
                        }
                    }
                }

                // Date + time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(0.dp) // invisible spacer
                    )
                    Text(
                        text = "📅 ${event.date}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Joined count row — live updated from bookings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(minOf(3, ticketsSold.coerceAtLeast(1))) { i ->
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .offset(x = (-i * 6).dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (i) {
                                            0 -> Color(0xFFBBDEFB)
                                            1 -> Color(0xFF90CAF9)
                                            else -> Color(0xFF64B5F6)
                                        }
                                    )
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "$ticketsSold/$ticketsTotal Joined",
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    // Available tickets badge — turns red when low
                    val badgeText: String
                    val badgeColor: Color
                    if (event.status == "draft") {
                        badgeText = "Draft"
                        badgeColor = Color(0xFF757575) // Neutral gray
                    } else if (ticketsAvailable == 0) {
                        badgeText = "Sold Out"
                        badgeColor = Color(0xFFD32F2F)
                    } else if (ticketsAvailable <= (ticketsTotal * 0.1f).coerceAtLeast(3f)) {
                        badgeText = "$ticketsAvailable left"
                        badgeColor = Color(0xFFFF6F00)
                    } else {
                        badgeText = "$ticketsAvailable left"
                        badgeColor = Color(0xFF388E3C)
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Progress label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("REGISTRATION PROGRESS", color = Color.Gray, fontSize = 10.sp, letterSpacing = 0.5.sp)
                    Text("$progressPercent%", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (ticketsAvailable == 0) Color(0xFFD32F2F) else Color(0xFF1976D2),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Action Buttons: Manage, View Details, and Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("Delete", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onManage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Manage", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                    ) {
                        Text("View Details", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
