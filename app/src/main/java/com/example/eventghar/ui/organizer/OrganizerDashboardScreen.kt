@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.eventghar.ui.organizer

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventghar.ui.profile.UserProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image

@Composable
fun OrganizerDashboardScreen(navController: NavController, isDarkTheme: Boolean = false, onThemeToggle: () -> Unit = {}) {
    // OrganizerBottomNavigationBar IS the full organizer dashboard —
    // it contains the BottomAppBar + FAB + all tabs (Home, My Events, Analytics, Settings)
    OrganizerBottomNavigationBar(
        navController = navController,
        isDarkTheme = isDarkTheme,
        onThemeToggle = onThemeToggle
    )
}

@Composable
fun SettingsMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(text, fontWeight = FontWeight.Medium, fontSize = 17.sp, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFB0B8C1), modifier = Modifier.size(18.dp))
    }
}

@Suppress("unused")
@Composable
fun OrganizerSettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )
    val userProfile by userProfileViewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar with back button
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Settings",
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(Modifier.width(48.dp)) // Balance for the back button
        }

        // Profile Card
        Row(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (userProfile.profileImageUri.isNotEmpty()) {
                    val imgModel2: Any = run {
                        val f = java.io.File(userProfile.profileImageUri)
                        if (f.exists()) f else userProfile.profileImageUri
                    }
                    AsyncImage(
                        model = imgModel2,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(userProfile.name.ifEmpty { "Organizer" }, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Verified Organizer", color = Color(0xFF2196F3), fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier
                        .background(Color(0x1A2196F3), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp))
                }
                Text(userProfile.email.ifEmpty { "organizer@eventghar.com" }, color = Color.Gray, fontSize = 15.sp)
            }
            // Online status dot
            Box(
                Modifier
                    .align(Alignment.Bottom)
                    .offset(x = (-8).dp, y = 8.dp)
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
            }
        }
        // Account Management
        Text("ACCOUNT MANAGEMENT", color = Color(0xFFB0B8C1), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 4.dp))
        SettingsMenuItem(
            icon = Icons.Filled.Edit,
            iconBg = Color(0xFFE3EAF2),
            text = "Edit Profile",
            onClick = { navController.navigate("edit_profile") }
        )
        SettingsMenuItem(
            icon = Icons.Filled.Wallet,
            iconBg = Color(0xFFDDF7E3),
            text = "Payment Methods",
            onClick = { /* TODO: Implement payment methods navigation */ }
        )
        // App Settings
        Text("APP SETTINGS", color = Color(0xFFB0B8C1), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 4.dp))
        SettingsMenuItem(
            icon = Icons.Filled.Notifications,
            iconBg = Color(0xFFFFF3E0),
            text = "Notification Preferences",
            onClick = { /* TODO: Implement notification preferences navigation */ }
        )
        SettingsMenuItem(
            icon = Icons.AutoMirrored.Filled.Help,
            iconBg = Color(0xFFEDE7F6),
            text = "Help & Support",
            onClick = { /* TODO: Implement help & support navigation */ }
        )
        // Logout
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .background(Color(0xFFFFEBEE), RoundedCornerShape(16.dp))
                .clickable {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(Modifier.width(8.dp))
            Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(16.dp))
        // Version
        Text(
            "Version 2.4.1 (EventGhar Organizer Pro)",
            color = Color(0xFFB0B8C1),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
