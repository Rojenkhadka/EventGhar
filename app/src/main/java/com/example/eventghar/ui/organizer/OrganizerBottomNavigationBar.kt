package com.example.eventghar.ui.organizer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.graphicsLayer
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.eventghar.ui.profile.UserProfileViewModel

@Composable
fun OrganizerBottomNavigationBar(navController: NavController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "My Events", "Analytics", "Settings")
    val icons = listOf(Icons.Default.Home, Icons.Default.Event, Icons.Default.Analytics, Icons.Default.Settings)

    // Event CRUD state
    val eventViewModel: EventViewModel = viewModel()
    val profileViewModel: UserProfileViewModel = viewModel()
    val showDialog = remember { mutableStateOf(false) }
    val editEvent = remember { mutableStateOf<Event?>(null) }

    Scaffold(
        bottomBar = {
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
        },
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            when (selectedItem) {
                0 -> {
                    // Home: event list
                    LazyColumn {
                        items(eventViewModel.events) { event ->
                            EventCard(
                                event = event,
                                onEdit = {
                                    editEvent.value = event
                                    showDialog.value = true
                                },
                                onDelete = { eventViewModel.deleteEvent(event.id) }
                            )
                        }
                    }
                    if (showDialog.value) {
                        EventDialog(
                            event = editEvent.value,
                            onDismiss = { showDialog.value = false },
                            onSave = { event ->
                                if (event.id == 0) eventViewModel.addEvent(event)
                                else eventViewModel.updateEvent(event)
                                showDialog.value = false
                            }
                        )
                    }
                }
                1 -> {
                    // My Events: same event list with FAB for creating
                    Box(Modifier.fillMaxSize()) {
                        LazyColumn {
                            items(eventViewModel.events) { event ->
                                EventCard(
                                    event = event,
                                    onEdit = {
                                        editEvent.value = event
                                        showDialog.value = true
                                    },
                                    onDelete = { eventViewModel.deleteEvent(event.id) }
                                )
                            }
                        }
                        FloatingActionButton(
                            onClick = { showDialog.value = true; editEvent.value = null },
                            shape = CircleShape,
                            containerColor = Color.Blue,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Filled.Add, "New Event")
                        }
                        if (showDialog.value) {
                            EventDialog(
                                event = editEvent.value,
                                onDismiss = { showDialog.value = false },
                                onSave = { event ->
                                    if (event.id == 0) eventViewModel.addEvent(event)
                                    else eventViewModel.updateEvent(event)
                                    showDialog.value = false
                                }
                            )
                        }
                    }
                }
                2 -> {
                    // Analytics placeholder
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Analytics coming soon")
                    }
                }
                3 -> {
                    // Settings
                    OrganizerSettingsScreen(navController = navController, profileViewModel = profileViewModel)
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onEdit: () -> Unit, onDelete: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Box(Modifier.padding(16.dp)) {
            Text(text = event.title)
            // Add more event details and edit/delete buttons as needed
            Button(onClick = onEdit, modifier = Modifier.align(Alignment.TopEnd)) { Text("Edit") }
            Button(onClick = onDelete, modifier = Modifier.align(Alignment.BottomEnd)) { Text("Delete") }
        }
    }
}

@Composable
fun EventDialog(event: Event?, onDismiss: () -> Unit, onSave: (Event) -> Unit) {
    val title = remember { mutableStateOf(event?.title ?: "") }
    val date = remember { mutableStateOf(event?.date ?: "") }
    val location = remember { mutableStateOf(event?.location ?: "") }
    val description = remember { mutableStateOf(event?.description ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (event == null) "Add Event" else "Edit Event") },
        text = {
            Box {
                OutlinedTextField(value = title.value, onValueChange = { title.value = it }, label = { Text("Title") })
                OutlinedTextField(value = date.value, onValueChange = { date.value = it }, label = { Text("Date") })
                OutlinedTextField(value = location.value, onValueChange = { location.value = it }, label = { Text("Location") })
                OutlinedTextField(value = description.value, onValueChange = { description.value = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    Event(
                        id = event?.id ?: 0,
                        title = title.value,
                        date = date.value,
                        location = location.value,
                        description = description.value
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
    val categoryOptions = listOf("Music", "Conference", "Workshop", "Party")
    val date = remember { mutableStateOf("") }
    val time = remember { mutableStateOf("") }
    val venue = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val capacity = remember { mutableStateOf("") }
    val isFree = remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Create Event",
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Save Draft",
                Modifier.clickable {
                    onSaveDraft(
                        Event(
                            title = eventName.value,
                            date = date.value,
                            location = venue.value,
                            description = ""
                        )
                    )
                },
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium
            )
        }
        // Progress indicator (fake)
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { idx ->
                Box(
                    Modifier
                        .size(48.dp, 6.dp)
                        .background(
                            if (idx == 0) Color(0xFF1976D2) else Color(0xFFE3EAF2),
                            RoundedCornerShape(3.dp)
                        )
                        .padding(horizontal = 2.dp)
                )
            }
        }
        // Cover photo picker (placeholder)
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .clickable { /* TODO: Pick image */ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                Text("Change Cover Photo", color = Color.White)
            }
        }
        // Event Name
        OutlinedTextField(
            value = eventName.value,
            onValueChange = { eventName.value = it },
            label = { Text("Event Name") },
            placeholder = { Text("e.g. Summer Music Festival 2024") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        // Category dropdown
        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = category.value,
                onValueChange = {},
                label = { Text("Category") },
                placeholder = { Text("Select Category") },
                modifier = Modifier.fillMaxWidth().clickable { expanded.value = true },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, Modifier.graphicsLayer { rotationZ = 90f })
                }
            )
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                categoryOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category.value = option
                            expanded.value = false
                        }
                    )
                }
            }
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
                }
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
                }
            )
        }
        // Venue Location
        OutlinedTextField(
            value = venue.value,
            onValueChange = { venue.value = it },
            label = { Text("Venue Location") },
            placeholder = { Text("Search address or venue name") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFFE3EAF2), RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
        }
        // Ticket Information
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFE3EAF2), RoundedCornerShape(8.dp))
                .padding(8.dp)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF1976D2))
            Text("Ticket Information", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            Spacer(Modifier.weight(1f))
            Text("FREE EVENT", color = Color.Gray)
            Switch(checked = isFree.value, onCheckedChange = { isFree.value = it })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = price.value,
                onValueChange = { price.value = it },
                label = { Text("Price") },
                placeholder = { Text("$ 0.00") },
                modifier = Modifier.weight(1f),
                enabled = !isFree.value
            )
            OutlinedTextField(
                value = capacity.value,
                onValueChange = { capacity.value = it },
                label = { Text("Capacity") },
                placeholder = { Text("Unlimited") },
                modifier = Modifier.weight(1f)
            )
        }
        // Publish Button
        Button(
            onClick = {
                onPublish(
                    Event(
                        title = eventName.value,
                        date = date.value,
                        location = venue.value,
                        description = ""
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Publish Event", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
