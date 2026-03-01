package com.example.eventghar.ui.organizer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventghar.data.StorageUtil
import kotlinx.coroutines.launch
import java.util.Calendar
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun OrganizerManageEventScreen(
    event: Event,
    onBack: () -> Unit,
    onSave: (Event) -> Unit
) {
    // Intercept system back button
    BackHandler { onBack() }

    val context = LocalContext.current
    var eventName by remember { mutableStateOf(event.title) }
    // Pre-populate category from existing event
    var category by remember { mutableStateOf(event.category) }
    var expanded by remember { mutableStateOf(false) }
    val categoryOptions = listOf("Workshop", "Seminar", "Conference", "Concert", "Cultural Event", "Sports", "Tech Event", "Other")
    var isCustomCategory by remember { mutableStateOf(event.category.isNotBlank() && !categoryOptions.dropLast(1).contains(event.category)) }
    var date by remember { mutableStateOf(event.date) }
    // Pre-populate time from existing event
    var time by remember { mutableStateOf(event.time) }
    var venue by remember { mutableStateOf(event.location) }
    // Pre-populate price and capacity from existing event
    var price by remember { mutableStateOf(event.price) }
    var capacity by remember { mutableStateOf(if (event.ticketsTotal > 0) event.ticketsTotal.toString() else "") }

    // Validation error states
    var eventNameError by remember { mutableStateOf("") }
    var categoryError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }
    var timeError by remember { mutableStateOf("") }
    var venueError by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf("") }
    var capacityError by remember { mutableStateOf("") }

    fun validate(): Boolean {
        var valid = true
        eventNameError = if (eventName.trim().length < 3) "Event name must be at least 3 characters" else ""
        if (eventNameError.isNotEmpty()) valid = false

        categoryError = if (category.isBlank() || category.trim().length < 3) "Please select or enter a category" else ""
        if (categoryError.isNotEmpty()) valid = false

        dateError = if (date.isBlank()) "Please select event date" else ""
        if (dateError.isNotEmpty()) valid = false

        timeError = if (time.isBlank()) "Please select event time" else ""
        if (timeError.isNotEmpty()) valid = false

        venueError = if (venue.trim().length < 3) "Please enter event venue" else ""
        if (venueError.isNotEmpty()) valid = false

        priceError = if (price.isBlank() || price.toFloatOrNull() == null || price.toFloat() <= 0) "Please enter valid ticket price" else ""
        if (priceError.isNotEmpty()) valid = false

        capacityError = if (capacity.isBlank() || capacity.toIntOrNull() == null || capacity.toInt() <= 0) "Please enter valid capacity" else ""
        if (capacityError.isNotEmpty()) valid = false

        return valid
    }

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
                "Edit Event Details",
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }

        // Cover photo picker
        var coverImageUri by remember { mutableStateOf(event.coverImageUri ?: "") }
        // uploadedImageUrl: HTTPS URL from Firebase Storage (empty = no new image uploaded yet)
        var uploadedImageUrl by remember { mutableStateOf(if (StorageUtil.isRemoteUrl(event.coverImageUri)) event.coverImageUri ?: "" else "") }
        var isUploadingImage by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
            if (uri != null) {
                var localPath: String? = null
                try {
                    val input = context.contentResolver.openInputStream(uri)
                    val tmpFile = java.io.File(context.cacheDir, "pick_manage_${System.currentTimeMillis()}.jpg")
                    input?.use { it.copyTo(tmpFile.outputStream()) }
                    localPath = tmpFile.absolutePath
                } catch (e: Exception) { localPath = null }
                coverImageUri = localPath ?: uri.toString()
                uploadedImageUrl = ""
                isUploadingImage = true
                val pathToUpload = localPath ?: uri.toString()
                coroutineScope.launch {
                    val url = StorageUtil.uploadImage(context, "event_images", pathToUpload)
                    uploadedImageUrl = url ?: ""
                    isUploadingImage = false
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE3EAF2))
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            // Show current image as background if available
            if (coverImageUri.isNotEmpty()) {
                AsyncImage(
                    model = coverImageUri,
                    contentDescription = "Event Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // Always show the upload overlay (transparent if image exists, solid if not)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (coverImageUri.isNotEmpty())
                            Color.Black.copy(alpha = 0.35f)
                        else
                            Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (coverImageUri.isNotEmpty()) Color.White else Color(0xFF1976D2),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (coverImageUri.isNotEmpty()) "Change Event Banner" else "Upload Event Banner",
                        color = if (coverImageUri.isNotEmpty()) Color.White else Color(0xFF1976D2),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Tap to select from gallery",
                        color = if (coverImageUri.isNotEmpty()) Color.White.copy(alpha = 0.85f) else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Event Name
        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it; eventNameError = "" },
            label = { Text("Event Name") },
            placeholder = { Text("e.g. Summer Music Festival 2024") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            isError = eventNameError.isNotEmpty()
        )
        if (eventNameError.isNotEmpty()) Text(eventNameError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))

        // Category dropdown
        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = category,
                onValueChange = { if (isCustomCategory) { category = it; categoryError = "" } },
                label = { Text("Category") },
                placeholder = { Text("Select event category") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = !isCustomCategory,
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                    }
                },
                isError = categoryError.isNotEmpty()
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(320.dp)
            ) {
                categoryOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            if (option == "Other") {
                                category = ""
                                isCustomCategory = true
                            } else {
                                category = option
                                isCustomCategory = false
                            }
                            categoryError = ""
                            expanded = false
                        }
                    )
                }
            }
        }
        if (categoryError.isNotEmpty()) Text(categoryError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))

        // Date and Time
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it; dateError = "" },
                label = { Text("Date") },
                placeholder = { Text("mm/dd/yyyy") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            date = "%02d/%02d/%04d".format(m + 1, d, y)
                            dateError = ""
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    })
                },
                isError = dateError.isNotEmpty()
            )
            OutlinedTextField(
                value = time,
                onValueChange = { time = it; timeError = "" },
                label = { Text("Start Time") },
                placeholder = { Text("--:--") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.clickable {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(context, { _, h, min ->
                            time = "%02d:%02d".format(h, min)
                            timeError = ""
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    })
                },
                isError = timeError.isNotEmpty()
            )
        }
        if (dateError.isNotEmpty()) Text(dateError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        if (timeError.isNotEmpty()) Text(timeError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))

        // Venue Location
        OutlinedTextField(
            value = venue,
            onValueChange = { venue = it; venueError = "" },
            label = { Text("Venue Location") },
            placeholder = { Text("Search address or venue name") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            isError = venueError.isNotEmpty()
        )
        if (venueError.isNotEmpty()) Text(venueError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))

        // Ticket Information Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
                    value = price,
                    onValueChange = { price = it; priceError = "" },
                    label = { Text("Price") },
                    placeholder = { Text("Rs 0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("Rs", fontWeight = FontWeight.Bold) },
                    isError = priceError.isNotEmpty()
                )
                if (priceError.isNotEmpty()) Text(priceError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it; capacityError = "" },
                    label = { Text("Capacity") },
                    placeholder = { Text("e.g. 100") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                    isError = capacityError.isNotEmpty()
                )
                if (capacityError.isNotEmpty()) Text(capacityError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        // Save Button — saves ALL fields
        Button(
            onClick = {
                if (validate()) {
                    onSave(
                        Event(
                            id = event.id,
                            title = eventName,
                            category = category,
                            date = date,
                            time = time,
                            location = venue,
                            description = event.description,
                            price = price,
                            status = event.status,
                            organizerId = event.organizerId,
                            coverImageUri = if (uploadedImageUrl.isNotEmpty()) uploadedImageUrl else (event.coverImageUri ?: ""),
                            ticketsSold = event.ticketsSold,
                            ticketsTotal = capacity.toIntOrNull() ?: event.ticketsTotal
                        )
                    )
                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Text("Save Changes", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun ManageActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF1976D2))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun EventCardWithDelete(
    event: Event,
    onManage: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit // new callback
) {
    val showDialog = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // ...existing event card content (title, date, progress bar, etc)...
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onManage, modifier = Modifier.padding(end = 8.dp)) {
                    Text("Manage")
                }
                Button(onClick = onViewDetails, modifier = Modifier.padding(end = 8.dp)) {
                    Text("View Details")
                }
                Button(
                    onClick = { showDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to delete this event? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog.value = false
                    onDelete()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val imagesDir = File(context.filesDir, "event_images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val fileName = "event_${System.currentTimeMillis()}.jpg"
        val file = File(imagesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file.absolutePath // return the file path
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
