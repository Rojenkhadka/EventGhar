package com.example.eventghar.ui.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eventghar.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun LoginScreen(navController: NavController, isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    // Role picker shown when user exists in Auth but not in Firestore users collection
    var showRolePicker by remember { mutableStateOf(false) }
    var pendingUserId by remember { mutableStateOf("") }
    var pendingUserEmail by remember { mutableStateOf("") }
    var pendingUserName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    val focusManager = LocalFocusManager.current

    // Role picker dialog — shown when no Firestore document exists for this user
    if (showRolePicker) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Select Your Role", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = {
                Column {
                    Text("Your account profile was not found. Please select your role to continue:")
                    Spacer(Modifier.height(8.dp))
                    Text("This only happens once for existing accounts.", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showRolePicker = false
                    val profileData = hashMapOf(
                        "name" to pendingUserName,
                        "email" to pendingUserEmail,
                        "role" to "Organizer",
                        "phone" to "",
                        "profileImageUri" to ""
                    )
                    db.collection("users").document(pendingUserId).set(profileData)
                    navController.navigate("organizer_dashboard") { popUpTo("login") { inclusive = true } }
                }) { Text("I'm an Organizer") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showRolePicker = false
                    val profileData = hashMapOf(
                        "name" to pendingUserName,
                        "email" to pendingUserEmail,
                        "role" to "User",
                        "phone" to "",
                        "profileImageUri" to ""
                    )
                    db.collection("users").document(pendingUserId).set(profileData)
                    navController.navigate("user_dashboard") { popUpTo("login") { inclusive = true } }
                }) { Text("I'm a User") }
            }
        )
    }

    val onLogin = {
        focusManager.clearFocus()
        if (email == "admin@eventghar.com" && password == "admin123") {
            navController.navigate("admin_dashboard") { popUpTo("login") { inclusive = true } }
        } else {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            if (!isConnected) {
                emailError = "No internet connection"
                passwordError = " "
            } else {
                var isFormValid = true
                if (email.isBlank()) {
                    emailError = "Email cannot be empty"
                    isFormValid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = "Invalid email format"
                    isFormValid = false
                }

                if (password.isBlank()) {
                    passwordError = "Password cannot be empty"
                    isFormValid = false
                }

                if (isFormValid) {
                    isLoading = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                if (user != null) {
                                    db.collection("users").document(user.uid).get()
                                        .addOnSuccessListener { document ->
                                            isLoading = false
                                            val role = document?.getString("role") ?: ""
                                            Log.d("LoginScreen", "uid=${user.uid} role='$role' docExists=${document?.exists()}")

                                            if (role == "Organizer") {
                                                navController.navigate("organizer_dashboard") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else if (role == "User") {
                                                navController.navigate("user_dashboard") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else {
                                                // No Firestore document — show role picker so user can identify themselves
                                                pendingUserId = user.uid
                                                pendingUserEmail = user.email ?: email
                                                pendingUserName = user.displayName ?: email.substringBefore("@")
                                                showRolePicker = true
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Log.e("LoginScreen", "Firestore role fetch failed: ${e.message}", e)
                                            // Firestore read failed — show role picker so user can still log in
                                            pendingUserId = user.uid
                                            pendingUserEmail = user.email ?: email
                                            pendingUserName = user.displayName ?: email.substringBefore("@")
                                            showRolePicker = true
                                        }
                                } else {
                                    isLoading = false
                                }
                            } else {
                                isLoading = false
                                val exception = task.exception
                                Log.e("LoginScreen", "Login failed", exception)
                                emailError = "Check your email and password"
                                passwordError = " "
                            }
                        }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.eventghar),
                        contentDescription = "App Logo",
                        modifier = Modifier.height(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon"
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = emailError != null,
                        supportingText = {
                            emailError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon"
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, "toggle password visibility")
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onLogin() }),
                        isError = passwordError != null,
                        supportingText = {
                            passwordError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { navController.navigate("forgot_password") },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text("Forgot Password?")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onLogin() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign In", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = { navController.navigate("registration") }) {
                        Text("Don't have an account? Sign Up")
                    }
                }
            }
        }
    }
}
