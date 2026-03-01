package com.example.eventghar.ui.organizer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventghar.data.UserProfile
import com.example.eventghar.ui.profile.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun OrganizerSettingsScreen(
    navController: NavController,
    userProfile: UserProfile,
    isDarkTheme: Boolean = false,
    onThemeToggle: () -> Unit = {}
) {
    val context = LocalContext.current
    // Use the same ViewModel instance as the parent — scoped to the Activity
    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )
    // Collect live profile from Firestore — this is the single source of truth
    val liveProfile by userProfileViewModel.userProfile.collectAsState()

    // Trigger a Firestore refresh every time this screen is shown
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (!uid.isNullOrBlank()) {
            userProfileViewModel.loadUserProfileFromFirestore(uid)
        }
    }

    // Always prefer the live Firestore data; fall back to passed-in userProfile
    val profileImageUri = liveProfile.profileImageUri.ifBlank { userProfile.profileImageUri }
    val displayName = liveProfile.name.ifBlank {
        userProfile.name.ifBlank { FirebaseAuth.getInstance().currentUser?.displayName ?: "" }
    }
    val displayEmail = liveProfile.email.ifBlank {
        userProfile.email.ifBlank { FirebaseAuth.getInstance().currentUser?.email ?: "" }
    }
    val isVerified = liveProfile.isVerified || userProfile.isVerified

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
                        visualTransformation = PasswordVisualTransformation(),
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
                        visualTransformation = PasswordVisualTransformation(),
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
                        visualTransformation = PasswordVisualTransformation(),
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
                                            Toast.makeText(
                                                context,
                                                "✅ Password changed successfully!",
                                                Toast.LENGTH_LONG
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
        // ─── Profile Header Card ─────────────────────────────────────────────
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
                // Avatar (click to open Edit Profile — changing photo only available there)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { navController.navigate("edit_profile") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri.isNotEmpty()) {
                        val imgModel = com.example.eventghar.ui.common.resolveImageModel(profileImageUri)
                        if (imgModel != null) {
                            AsyncImage(
                                model = imgModel,
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
                    if (isVerified) {
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
                                text = "Verified Organizer",
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
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ─── Account Section ─────────────────────────────────────────────────
        SettingsSectionHeader(title = "ACCOUNT MANAGEMENT")

        SettingsItem(
            icon = Icons.Default.Person,
            iconTint = Color(0xFF1976D2),
            iconBackground = Color(0xFFE3F2FD),
            title = "Edit Profile",
            subtitle = "Change your name, phone and photo",
            onClick = { navController.navigate("edit_profile") }
        )

        SettingsItem(
            icon = Icons.Default.Lock,
            iconTint = Color(0xFF7B1FA2),
            iconBackground = Color(0xFFF3E5F5),
            title = "Change Password",
            subtitle = "Update your login password",
            onClick = { showChangePasswordDialog = true }
        )

        // ─── Preferences Section ─────────────────────────────────────────────
        SettingsSectionHeader(title = "Preferences")

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

        // ─── Logout ──────────────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
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
private fun SettingsItem(
    icon: ImageVector,
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
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
