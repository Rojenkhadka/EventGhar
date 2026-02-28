package com.example.eventghar.ui.organizer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.example.eventghar.data.StorageUtil
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.example.eventghar.data.UserProfile
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import com.example.eventghar.ui.profile.UserProfileViewModel
import java.util.Calendar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.ButtonDefaults
import android.content.Context
import android.widget.Toast
import java.io.InputStream
import java.io.File
import java.io.FileOutputStream
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.eventghar.data.BookingDataStore
import com.example.eventghar.ui.user.Booking


@Composable
fun OrganizerBottomNavigationBar(navController: androidx.navigation.NavController, isDarkTheme: Boolean = false, onThemeToggle: () -> Unit = {}) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf("Home", "My Events", "Analytics", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Event, Icons.Default.Analytics, Icons.Default.Settings)


    // Navigation state: "dashboard", "create", or "manage"
    var navState by remember { mutableStateOf("dashboard") }
    var selectedEventForManagement by remember { mutableStateOf<Event?>(null) }

    // Event CRUD state
    val localContext = LocalContext.current
    // Event CRUD state (AndroidViewModel requires a factory)
    val eventViewModel: EventViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(localContext.applicationContext as Application))
    // --- Inject UserProfileViewModel ---
    val userProfileViewModel: UserProfileViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(localContext.applicationContext as Application))
    val userProfile by userProfileViewModel.userProfile.collectAsState()

    when (navState) {
        "create" -> {
            CreateEventScreen(
                onBack = { navState = "dashboard" },
                onPublish = { event ->
                    eventViewModel.addEvent(event)
                    Toast.makeText(localContext, "🎉 Event published successfully!", Toast.LENGTH_SHORT).show()
                    navState = "dashboard"
                },
                onSaveDraft = { event ->
                    eventViewModel.addEvent(event)
                    Toast.makeText(localContext, "📝 Event saved as draft.", Toast.LENGTH_SHORT).show()
                    navState = "dashboard"
                }
            )
        }
        "manage" -> {
            selectedEventForManagement?.let { event ->
                OrganizerManageEventScreen(
                    event = event,
                    onBack = { navState = "dashboard" },
                    onSave = { updatedEvent ->
                        eventViewModel.updateEvent(updatedEvent)
                        Toast.makeText(localContext, "Event changes saved.", Toast.LENGTH_SHORT).show()
                        navState = "dashboard"
                    }
                )
            } ?: run { navState = "dashboard" }
        }
        "viewDetails" -> {
            selectedEventForManagement?.let { event ->
                OrganizerEventDetailsScreen(
                    event = event,
                    onBack = { navState = "dashboard" }
                )
            } ?: run { navState = "dashboard" }
        }
        else -> {
            Scaffold(
                bottomBar = {
                    Box(Modifier.fillMaxWidth()) {
                        BottomAppBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                icon = { Icon(icons[0], contentDescription = items[0]) },
                                label = { Text(items[0]) },
                                selected = selectedItem == 0,
                                onClick = { selectedItem = 0 },
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            NavigationBarItem(
                                icon = { Icon(icons[1], contentDescription = items[1]) },
                                label = { Text(items[1]) },
                                selected = selectedItem == 1,
                                onClick = { selectedItem = 1 },
                                modifier = Modifier.padding(top = 12.dp)
                            )

                            Spacer(Modifier.weight(1f, fill = true))

                            NavigationBarItem(
                                icon = { Icon(icons[2], contentDescription = items[2]) },
                                label = { Text(items[2]) },
                                selected = selectedItem == 2,
                                onClick = { selectedItem = 2 },
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            NavigationBarItem(
                                icon = { Icon(icons[3], contentDescription = items[3]) },
                                label = { Text(items[3]) },
                                selected = selectedItem == 3,
                                onClick = { selectedItem = 3 },
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                        FloatingActionButton(
                            onClick = {
                                navState = "create"
                            },
                            shape = CircleShape,
                            containerColor = Color.Blue,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-28).dp)
                                .zIndex(1f)
                        ) {
                            Icon(Icons.Filled.Add, "New Event")
                        }
                    }
                },
            ) { paddingValues ->
                Box(Modifier.padding(paddingValues)) {
                    // collect organizer events once at top-level so home + my-events share the same source
                    val organizerEvents by eventViewModel.events.collectAsState()
                    // Show dashboard home page in Home tab
                    if (selectedItem == 0) {
                        OrganizerDashboardHomePage(userProfile = userProfile, events = organizerEvents)
                    }
                    // Only show events in My Events tab
                    if (selectedItem == 1) {
                        val events = organizerEvents
                        // Add state for error bar for publish validation
                        val showErrorBar = remember { mutableStateOf(false) }
                        val errorMessage = remember { mutableStateOf("") }

                        fun validateEvent(event: Event): Boolean {
                            // Use the same validation as CreateEventScreen
                            if (event.title.trim().length < 3) {
                                errorMessage.value = "Event name must be at least 3 characters"
                                return false
                            }
                            if (event.category.isBlank() || event.category.trim().length < 3) {
                                errorMessage.value = "Please select or enter a category"
                                return false
                            }
                            if (event.date.isBlank()) {
                                errorMessage.value = "Please select event date"
                                return false
                            }
                            if (event.time.isBlank()) {
                                errorMessage.value = "Please select event time"
                                return false
                            }
                            if (event.location.trim().length < 3) {
                                errorMessage.value = "Please enter event venue"
                                return false
                            }
                            if (event.price.isBlank() || event.price.toFloatOrNull() == null || event.price.toFloat() <= 0) {
                                errorMessage.value = "Please enter valid ticket price"
                                return false
                            }
                            if (event.ticketsTotal <= 0) {
                                errorMessage.value = "Please enter valid capacity"
                                return false
                            }
                            return true
                        }

                        fun publishDraftEvent(event: Event) {
                            if (validateEvent(event)) {
                                val publishedEvent = event.copy(status = "published")
                                eventViewModel.updateEvent(publishedEvent)
                                showErrorBar.value = false
                                Toast.makeText(localContext, "Event \"${event.title}\" published successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                showErrorBar.value = true
                            }
                        }

                        OrganizerMyEventsScreen(
                            events = events,
                            onManage = { event ->
                                selectedEventForManagement = event
                                navState = "manage"
                            },
                            onDelete = { id ->
                                val deletedEvent = events.find { it.id == id }
                                eventViewModel.deleteEvent(id)
                                Toast.makeText(localContext, "Event \"${deletedEvent?.title ?: ""}\" deleted.", Toast.LENGTH_SHORT).show()
                            },
                            onViewDetails = { event ->
                                selectedEventForManagement = event
                                navState = "viewDetails"
                            },
                            onPublish = { event -> publishDraftEvent(event) }
                        )
                        // Show error bar if validation fails
                        if (showErrorBar.value) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .zIndex(2f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Incomplete Information",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            errorMessage.value,
                                            color = Color(0xFFD32F2F),
                                            fontSize = 12.sp
                                        )
                                    }
                                    IconButton(onClick = { showErrorBar.value = false }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Show Analytics page
                    if (selectedItem == 2) {
                        val allBookings by BookingDataStore.bookingsFlow(localContext).collectAsState(initial = emptyList())
                        OrganizerAnalyticsScreen(events = organizerEvents, allBookings = allBookings)
                    }
                    // Show Settings page
                    if (selectedItem == 3) {
                        OrganizerSettingsScreen(
                            navController = navController,
                            userProfile = userProfile,
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = onThemeToggle
                        )
                    }
                }
            }
        }
    }

    // Handle details view state
    if (navState == "details") {
        selectedEventForManagement?.let { event ->
            OrganizerEventDetailsScreen(
                event = event,
                onBack = { navState = "dashboard" }
            )
        }
    }
}

@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onPublish: (Event) -> Unit,
    onSaveDraft: (Event) -> Unit
) {
    val context = LocalContext.current
    val eventName = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(false) }
    val categoryOptions = listOf("Workshop", "Seminar", "Conference", "Concert", "Cultural Event", "Sports", "Tech Event", "Other")
    val date = remember { mutableStateOf("") }
    val time = remember { mutableStateOf("") }
    val venue = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val capacity = remember { mutableStateOf("") }
    var isCustomCategory by remember { mutableStateOf(false) }

    // coverImageUri holds the local preview path; uploadedImageUrl holds the Firebase Storage HTTPS URL
    var coverImageUri by remember { mutableStateOf<String?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Image picker — copies bytes to cacheDir immediately, then uploads to Firebase Storage in background
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Step 1: Copy bytes to a local temp file for immediate preview
            var localPath: String? = null
            try {
                val input = context.contentResolver.openInputStream(uri)
                val tmpFile = File(context.cacheDir, "pick_event_${System.currentTimeMillis()}.jpg")
                input?.use { it.copyTo(tmpFile.outputStream()) }
                localPath = tmpFile.absolutePath
            } catch (e: Exception) {
                localPath = null
            }
            coverImageUri = localPath ?: uri.toString()
            uploadedImageUrl = null // reset previous URL
            isUploadingImage = true

            // Step 2: Upload to Firebase Storage immediately in background
            val pathToUpload = localPath ?: uri.toString()
            coroutineScope.launch {
                val url = StorageUtil.uploadImage(context, "event_images", pathToUpload)
                uploadedImageUrl = url
                isUploadingImage = false
            }
        }
    }

    // Validation error states
    val eventNameError = remember { mutableStateOf("") }
    val categoryError = remember { mutableStateOf("") }
    val dateError = remember { mutableStateOf("") }
    val timeError = remember { mutableStateOf("") }
    val venueError = remember { mutableStateOf("") }
    val priceError = remember { mutableStateOf("") }
    val capacityError = remember { mutableStateOf("") }
    val showErrorBar = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    fun validate(): Boolean {
        var valid = true
        var firstError = ""

        eventNameError.value = if (eventName.value.trim().length < 3) "Event name must be at least 3 characters" else ""
        if (eventNameError.value.isNotEmpty()) {
            valid = false
            if (firstError.isEmpty()) firstError = "Please enter a valid event name"
        }

        categoryError.value = if (category.value.isBlank() || (isCustomCategory && category.value.trim().length < 3)) "Select a category or type at least 3 characters" else ""
        if (categoryError.value.isNotEmpty()) {
            valid = false
            if (firstError.isEmpty()) firstError = "Please select or enter a category"
        }

        dateError.value = if (date.value.isBlank()) "Date is required" else ""
        if (dateError.value.isNotEmpty()) {
            valid = false
            if (firstError.isEmpty()) firstError = "Please select event date"
        }

        timeError.value = if (time.value.isBlank()) "Time is required" else ""
        if (timeError.value.isNotEmpty()) {
            valid = false
            if (firstError.isEmpty()) firstError = "Please select event time"
        }

        venueError.value = if (venue.value.trim().length < 3) "Venue must be at least 3 characters" else ""
        if (venueError.value.isNotEmpty()) {
            valid = false
            if (firstError.isEmpty()) firstError = "Please enter event venue"
        }

        priceError.value = if (price.value.isBlank() || price.value.toFloatOrNull() == null || price.value.toFloat() <= 0) "Price must be a positive number" else ""
        if (priceError.value.isNotEmpty()) {
            valid = false
            if (firstError.isEmpty()) firstError = "Please enter valid ticket price"
        }

        capacityError.value = if (capacity.value.isBlank() || capacity.value.toIntOrNull() == null || capacity.value.toInt() <= 0) "Capacity must be a positive integer" else ""
        if (capacityError.value.isNotEmpty()) {
            valid = false
            if (firstError.isEmpty()) firstError = "Please enter valid capacity"
        }

        if (!valid) {
            errorMessage.value = firstError
            showErrorBar.value = true
        }

        return valid
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Top bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Create Event",
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        // Cover photo picker
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { if (!isUploadingImage) imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (coverImageUri != null) {
                val imageModel: Any? = com.example.eventghar.ui.common.resolveImageModel(coverImageUri)
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Event Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Overlay: show uploading spinner or tap-to-change
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isUploadingImage) 0.55f else 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingImage) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Uploading image...", color = Color.White, fontSize = 13.sp)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Text("Tap to change photo", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(44.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Upload Event Banner", color = Color(0xFF1976D2), fontWeight = FontWeight.SemiBold)
                    Text("Tap to select from gallery", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        // Event Name
        OutlinedTextField(
            value = eventName.value,
            onValueChange = { eventName.value = it },
            label = { Text("Event Name") },
            placeholder = { Text("e.g. Summer Music Festival 2024") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            isError = eventNameError.value.isNotEmpty()
        )
        if (eventNameError.value.isNotEmpty()) {
            Text(eventNameError.value, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }
        // Category dropdown - regular input style
        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = category.value,
                onValueChange = {
                    if (isCustomCategory) category.value = it
                },
                label = { Text("Category") },
                placeholder = { Text("Select event category") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = !isCustomCategory,
                trailingIcon = {
                    IconButton(onClick = { expanded.value = !expanded.value }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                    }
                },
                isError = categoryError.value.isNotEmpty()
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier.width(320.dp)
            ) {
                categoryOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            if (option == "Other") {
                                category.value = ""
                                isCustomCategory = true
                            } else {
                                category.value = option
                                isCustomCategory = false
                            }
                            expanded.value = false
                        }
                    )
                }
            }
        }
        if (categoryError.value.isNotEmpty()) {
            Text(categoryError.value, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }
        // Date and Time
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = date.value,
                onValueChange = { date.value = it },
                label = { Text("Date") },
                placeholder = { Text("mm/dd/yyyy") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.clickable {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                date.value = "%02d/%02d/%04d".format(month + 1, day, year)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    })
                },
                isError = dateError.value.isNotEmpty()
            )
            OutlinedTextField(
                value = time.value,
                onValueChange = { time.value = it },
                label = { Text("Start Time") },
                placeholder = { Text("--:--") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.clickable {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                time.value = "%02d:%02d".format(hour, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    })
                },
                isError = timeError.value.isNotEmpty()
            )
        }
        if (dateError.value.isNotEmpty()) {
            Text(dateError.value, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }
        if (timeError.value.isNotEmpty()) {
            Text(timeError.value, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }
        // Venue Location
        OutlinedTextField(
            value = venue.value,
            onValueChange = { venue.value = it },
            label = { Text("Venue Location") },
            placeholder = { Text("Search address or venue name") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            isError = venueError.value.isNotEmpty()
        )
        if (venueError.value.isNotEmpty()) {
            Text(venueError.value, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }
        // Modern Ticket Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF1976D2))
                    Text("Ticket Information", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = price.value,
                    onValueChange = { price.value = it },
                    label = { Text("Price") },
                    placeholder = { Text("Rs 0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("Rs", fontWeight = FontWeight.Bold) },
                    isError = priceError.value.isNotEmpty()
                )
                if (priceError.value.isNotEmpty()) {
                    Text(priceError.value, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = capacity.value,
                    onValueChange = { capacity.value = it },
                    label = { Text("Capacity") },
                    placeholder = { Text("Unlimited") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                    isError = capacityError.value.isNotEmpty()
                )
                if (capacityError.value.isNotEmpty()) {
                    Text(capacityError.value, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
            // Publish and Save Draft Buttons
            Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (isUploadingImage) return@Button
                        if (uploadedImageUrl.isNullOrBlank()) {
                            showErrorBar.value = true
                            errorMessage.value = "Please upload a cover image before publishing."
                            return@Button
                        }
                        if (validate()) {
                            onPublish(
                                Event(
                                    title = eventName.value,
                                    category = category.value,
                                    date = date.value,
                                    time = time.value,
                                    location = venue.value,
                                    description = "",
                                    price = price.value,
                                    ticketsTotal = capacity.value.toIntOrNull() ?: 0,
                                    status = "published",
                                    coverImageUri = uploadedImageUrl ?: ""
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    enabled = !isUploadingImage
                ) {
                    if (isUploadingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Uploading...")
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Publish Event", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Button(
                    onClick = {
                        if (isUploadingImage) return@Button
                        if (uploadedImageUrl.isNullOrBlank()) {
                            showErrorBar.value = true
                            errorMessage.value = "Please upload a cover image before saving draft."
                            return@Button
                        }
                        // Minimal validation for draft (only event name required)
                        if (eventName.value.trim().isNotEmpty()) {
                            onSaveDraft(
                                Event(
                                    title = eventName.value,
                                    category = category.value,
                                    date = date.value,
                                    time = time.value,
                                    location = venue.value,
                                    description = "",
                                    price = price.value,
                                    ticketsTotal = capacity.value.toIntOrNull() ?: 0,
                                    status = "draft",
                                    coverImageUri = uploadedImageUrl ?: ""
                                )
                            )
                        } else {
                            showErrorBar.value = true
                            errorMessage.value = "Event name is required to save draft."
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    enabled = !isUploadingImage
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Save as Draft", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        // Modern error bar at top
        if (showErrorBar.value) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Incomplete Information",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                        Text(
                            errorMessage.value,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { showErrorBar.value = false }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- Top Header Bar (simplified) ---
@Composable
fun OrganizerTopHeaderBar(
    userProfile: UserProfile,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val displayName = userProfile.name.ifBlank {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "Organizer"
    }

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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                val profileImgModel = com.example.eventghar.ui.common.resolveImageModel(
                    userProfile.profileImageUri.takeIf { it.isNotBlank() }
                )
                if (profileImgModel != null) {
                    AsyncImage(
                        model = profileImgModel,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
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

            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- Organizer Dashboard Home Page (refined) ---
@Composable
fun OrganizerDashboardHomePage(userProfile: UserProfile, events: List<Event>) {
    // More refined header/profile card. Show organizer's published upcoming events.
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {

        // Use the reusable top header bar for a consistent top header across screens
        OrganizerTopHeaderBar(
            userProfile = userProfile,
            onProfileClick = { /* TODO: navigate to profile/edit */ },
            onNotificationClick = { /* TODO: open notifications */ }
        )

        Spacer(Modifier.height(8.dp))

        // Upcoming events horizontal list - show only published events created by this organizer
        Text("Upcoming Events", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))

        val published = events.filter { it.status == "published" }

        if (published.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No upcoming events yet. Create one with the + button.", color = Color.Gray)
            }
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(published) { ev ->
                    // compute registration progress safely
                    val total = ev.ticketsTotal ?: 0
                    val sold = ev.ticketsSold ?: 0
                    val progress = if (total > 0) (sold.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
                    val dashEvent = DashboardEvent(
                        title = ev.title,
                        date = ev.date,
                        location = ev.location,
                        imageUrl = ev.coverImageUri,
                        registrationProgress = progress,
                        registrationText = if (progress > 0.0f) "${(progress * 100).toInt()}% booked" else "No registrations yet",
                        ticketsSold = sold,
                        ticketsTotal = total
                    )
                    DashboardEventCard(dashEvent, modifier = Modifier.width(260.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Recent Activity simplified
        Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Ticket sales increased by 15% this month", fontWeight = FontWeight.Medium)
                        Text("2 hrs ago", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// --- Dashboard Event Card ---
@Composable
fun DashboardEventCard(event: DashboardEvent, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()) {
                if (!event.imageUrl.isNullOrEmpty()) {
                    val model: Any? = com.example.eventghar.ui.common.resolveImageModel(event.imageUrl)
                    if (model != null) {
                        AsyncImage(model = model, contentDescription = "event image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                } else {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(36.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(event.title, fontWeight = FontWeight.Bold, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text("${event.date} • ${event.location}", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.LinearProgressIndicator(progress = { event.registrationProgress }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${event.ticketsSold}/${event.ticketsTotal} tickets", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { /* manage */ }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Manage", color = Color.White)
                    }
                }
            }
        }
    }
}

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val fileName = "event_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, fileName)
    inputStream?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}

// Data model for dashboard items (used by the home page previews)
data class DashboardEvent(
    val title: String,
    val date: String,
    val location: String,
    val imageUrl: String?,
    val registrationProgress: Float,
    val registrationText: String,
    val ticketsSold: Int,
    val ticketsTotal: Int
)
