package com.example.eventghar.ui.user

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventghar.data.UserProfile
import com.example.eventghar.ui.organizer.Event
import com.example.eventghar.ui.profile.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File

// Simple holder to pass Event to detail screen without complex navigation args
object UserEventDetailHolder {
    var event: Event? = null
}

@Composable
fun UserDashboardScreen(navController: NavController, isDarkTheme: Boolean = false, onThemeToggle: () -> Unit = {}) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf("Home", "Events", "My Bookings", "Settings")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Event,
        Icons.Default.BookOnline,
        Icons.Default.Settings
    )

    val localContext = LocalContext.current
    val publicEventViewModel: PublicEventViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(localContext.applicationContext as Application)
    )
    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(localContext.applicationContext as Application)
    )
    val userProfile by userProfileViewModel.userProfile.collectAsState()
    val publishedEvents by publicEventViewModel.publishedEvents.collectAsState()
    val myBookings by publicEventViewModel.myBookings.collectAsState()

    // Ensure we fetch the canonical registered profile from Firestore once when the dashboard is shown.
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (!uid.isNullOrBlank()) {
            try { userProfileViewModel.loadUserProfileFromFirestore(uid) } catch (_: Exception) {}
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            when (selectedItem) {
                0 -> UserHomeScreen(userProfile = userProfile, events = publishedEvents, navController = navController)
                1 -> UserEventsScreen(events = publishedEvents, navController = navController)
                2 -> UserBookingsScreen(bookings = myBookings)
                3 -> UserSettingsScreen(navController = navController, userProfile = userProfile, isDarkTheme = isDarkTheme, onThemeToggle = onThemeToggle)
            }
        }
    }
}

// --- User Home Screen ---
@Composable
fun UserHomeScreen(userProfile: UserProfile, events: List<Event>, navController: NavController) {
    // Use registered name from Firestore profile; fallback to Firebase Auth email prefix only if truly empty
    val displayName = userProfile.name.ifBlank {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        if (email.isNotBlank()) email.substringBefore("@") else "Loading..."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Bar
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val imgUri = userProfile.profileImageUri.takeIf { it.isNotBlank() }
                    if (imgUri != null) {
                        val imageModel: Any? = com.example.eventghar.ui.common.resolveImageModel(imgUri)
                        if (imageModel != null) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text("Hello,", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(displayName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                IconButton(onClick = { /* TODO: Handle notifications */ }) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Welcome Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "Welcome to EventGhar!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Discover and book amazing events happening around you",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Upcoming Events Section
        Text(
            "Upcoming Events",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        val publishedEvents = events.filter { it.status == "published" }

        if (publishedEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("No events available yet", color = Color.Gray)
                }
            }
        } else {
            publishedEvents.take(5).forEach { event ->
                UserEventCard(event = event, onClick = {
                    UserEventDetailHolder.event = event
                    navController.navigate("user_event_detail")
                })
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// --- User Events Screen ---
@Composable
fun UserEventsScreen(events: List<Event>, navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = events.filter {
        searchQuery.isBlank() ||
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.category.contains(searchQuery, ignoreCase = true) ||
        it.location.contains(searchQuery, ignoreCase = true)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "All Events",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search events...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        val publishedEvents = filtered

        if (publishedEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text(if (searchQuery.isBlank()) "No events available" else "No results for \"$searchQuery\"", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(publishedEvents) { event ->
                    UserEventCard(event = event, onClick = {
                        UserEventDetailHolder.event = event
                        navController.navigate("user_event_detail")
                    })
                }
            }
        }
    }
}

// --- User Bookings Screen ---
@Composable
fun UserBookingsScreen(bookings: List<Booking>) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(tonalElevation = 2.dp, shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("My Bookings", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("No bookings yet", color = Color.Gray, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Browse events and book your tickets!", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(bookings) { booking -> BookingCard(booking = booking) }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking) {
    var showTicket by remember { mutableStateOf(false) }

    if (showTicket) {
        BookingTicketDialog(booking = booking, onDismiss = { showTicket = false })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTicket = true },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail
            Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                val bookingImgModel = com.example.eventghar.ui.common.resolveImageModel(booking.coverImageUri)
                if (bookingImgModel != null) {
                    AsyncImage(model = bookingImgModel, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.eventTitle, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(booking.eventDate, fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(booking.eventLocation, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFFE8F5E9)) {
                        Text("${booking.ticketCount} ticket(s)", fontSize = 11.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Rs ${booking.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
            // Chevron arrow — same as Events tab cards
            Icon(Icons.Default.ChevronRight, contentDescription = "View Ticket", tint = Color.Gray)
        }
    }
}

@Composable
fun BookingTicketDialog(booking: Booking, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        // Use a custom full-width ticket layout
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Ticket Card ──────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column {
                        // Header banner / image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (booking.coverImageUri.isNotBlank()) {
                                val ticketImgModel = com.example.eventghar.ui.common.resolveImageModel(booking.coverImageUri)
                                if (ticketImgModel != null) {
                                    AsyncImage(
                                        model = ticketImgModel,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Semi-transparent overlay
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.38f))
                                    )
                                }
                            }
                            // Ticket icon + label always on top
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "EVENT TICKET",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        // ── Dashed separator with circles ─────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(20.dp).offset(x = (-10).dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Color.Gray.copy(alpha = 0.4f))
                            )
                            Box(
                                modifier = Modifier.size(20.dp).offset(x = 10.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }

                        // ── Ticket Details ────────────────────────────────
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text(
                                booking.eventTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                lineHeight = 24.sp
                            )
                            Spacer(Modifier.height(16.dp))

                            TicketRow(Icons.Default.CalendarToday, "Date", booking.eventDate)
                            Spacer(Modifier.height(10.dp))
                            TicketRow(Icons.Default.LocationOn, "Venue", booking.eventLocation)
                            Spacer(Modifier.height(10.dp))
                            TicketRow(Icons.Default.People, "Tickets", "${booking.ticketCount} ticket(s)")
                            Spacer(Modifier.height(10.dp))
                            TicketRow(
                                Icons.Default.Payments,
                                "Amount Paid",
                                if (booking.totalAmount == "0.00") "Free" else "Rs ${booking.totalAmount}"
                            )

                            Spacer(Modifier.height(16.dp))

                            // ── Confirmed badge ───────────────────────────
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Booking Confirmed",
                                        color = Color(0xFF388E3C),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        },
        title = null,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun TicketRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// --- User Settings Screen ---
@Composable
fun UserSettingsScreen(navController: NavController, userProfile: UserProfile, isDarkTheme: Boolean = false, onThemeToggle: () -> Unit = {}) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isChangingPassword by remember { mutableStateOf(false) }

    // Reset password fields when dialog opens
    LaunchedEffect(showChangePasswordDialog) {
        if (showChangePasswordDialog) {
            currentPassword = ""
            newPassword = ""
            confirmNewPassword = ""
            currentPasswordError = null
            newPasswordError = null
            confirmPasswordError = null
            isChangingPassword = false
        }
    }

    val profileImageUri = userProfile.profileImageUri
    val displayName = userProfile.name.ifBlank {
        FirebaseAuth.getInstance().currentUser?.displayName ?: ""
    }
    val displayEmail = userProfile.email.ifEmpty {
        FirebaseAuth.getInstance().currentUser?.email ?: ""
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        showLogoutDialog = false
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmNewPassword = ""
                currentPasswordError = null
                newPasswordError = null
                confirmPasswordError = null
            },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; currentPasswordError = null },
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (currentPasswordError != null) {
                        Text(currentPasswordError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; newPasswordError = null },
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (newPasswordError != null) {
                        Text(newPasswordError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it; confirmPasswordError = null },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (confirmPasswordError != null) {
                        Text(confirmPasswordError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentPasswordError = null
                        newPasswordError = null
                        confirmPasswordError = null
                        if (newPassword.length < 6) {
                            newPasswordError = "Password must be at least 6 characters"
                            return@Button
                        }
                        if (newPassword != confirmNewPassword) {
                            confirmPasswordError = "Passwords do not match"
                            return@Button
                        }
                        isChangingPassword = true
                        val user = FirebaseAuth.getInstance().currentUser
                        val email = user?.email
                        if (user != null && email != null) {
                            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            android.widget.Toast.makeText(
                                                context,
                                                "✅ Password changed successfully!",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmNewPassword = ""
                                            currentPasswordError = null
                                            newPasswordError = null
                                            confirmPasswordError = null
                                            isChangingPassword = false
                                            showChangePasswordDialog = false
                                        }
                                        .addOnFailureListener { e ->
                                            newPasswordError = e.localizedMessage ?: "Failed to change password"
                                            isChangingPassword = false
                                        }
                                }
                                .addOnFailureListener { e ->
                                    currentPasswordError = "Current password is incorrect"
                                    isChangingPassword = false
                                }
                        } else {
                            currentPasswordError = "User not logged in"
                            isChangingPassword = false
                        }
                    },
                    enabled = !isChangingPassword
                ) {
                    Text("Change Password")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showChangePasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                    currentPasswordError = null
                    newPasswordError = null
                    confirmPasswordError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ─── Profile Header Card (organizer-style, but for user) ─────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (click to open Edit Profile)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { navController.navigate("edit_profile") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri.isNotEmpty()) {
                        val settingsImgModel: Any = when {
                            profileImageUri.startsWith("http://") || profileImageUri.startsWith("https://") -> profileImageUri
                            profileImageUri.startsWith("content://") -> android.net.Uri.parse(profileImageUri)
                            File(profileImageUri).exists() -> File(profileImageUri)
                            else -> profileImageUri
                        }
                        AsyncImage(
                            model = settingsImgModel,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayEmail,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (userProfile.isVerified) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified",
                                fontSize = 12.sp,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { navController.navigate("edit_profile") }
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ─── Account Section ─────────────────────────────────────────────────
        UserSettingsSectionHeader(title = "ACCOUNT MANAGEMENT")

        UserSettingsItem(
            icon = Icons.Default.Person,
            iconTint = Color(0xFF1976D2),
            iconBackground = Color(0xFFE3F2FD),
            title = "Edit Profile",
            subtitle = "Change your name, phone and photo",
            onClick = { navController.navigate("edit_profile") }
        )

        UserSettingsItem(
            icon = Icons.Default.Lock,
            iconTint = Color(0xFF7B1FA2),
            iconBackground = Color(0xFFF3E5F5),
            title = "Change Password",
            subtitle = "Update your login password",
            onClick = { showChangePasswordDialog = true }
        )

        // ─── Preferences Section ─────────────────────────────────────────────
        UserSettingsSectionHeader(title = "PREFERENCES")

        // Dark Mode toggle card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 3.dp)
                .clickable { onThemeToggle() },
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFBE9E7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = Color(0xFFE64A19),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark Mode", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(if (isDarkTheme) "Dark theme is on" else "Light theme is on", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeToggle() }
                )
            }
        }

        // ─── Logout (with confirmation dialog) ──────────────────────────────
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.SemiBold)
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            showLogoutDialog = false
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Log Out")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun UserSettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp
    )
}

@Composable
private fun UserSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// --- User Event Card Component ---
@Composable
fun UserEventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val eventImgModel = com.example.eventghar.ui.common.resolveImageModel(event.coverImageUri)
                if (eventImgModel != null) {
                    AsyncImage(
                        model = eventImgModel,
                        contentDescription = "Event Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Event Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        event.date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        event.location,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Rs ${event.price}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
