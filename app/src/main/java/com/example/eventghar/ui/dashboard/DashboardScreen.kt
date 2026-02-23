package com.example.eventghar.ui.dashboard

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.eventghar.R
import com.example.eventghar.data.Event
import com.example.eventghar.ui.theme.EventGharTheme
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    val upcomingEvents = listOf(
        Event("AI Design Workshop", Date(), "A fun event for the whole community.", "Downtown Innovation Hub, Seattle", "49.00", R.drawable.eventghar_logo),
        Event("Jazz Under The Stars", Date(), "A conference about the latest in tech.", "Central Park Amphitheater", "35.00", R.drawable.eventghar_logo),
        Event("Morning Serenity Yoga Retreat", Date(), "Three days of live music.", "Botanical Gardens East", "Free", R.drawable.eventghar_logo)
    )

    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val onSearch = {
        focusManager.clearFocus()
        // TODO: Implement search logic here
        Log.d("DashboardScreen", "Search triggered for query: $searchQuery")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Welcome back,")
                        Text(FirebaseAuth.getInstance().currentUser?.displayName ?: "Alex Johnson", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle notification click */ }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search for events, venues...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Upcoming Events", style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(upcomingEvents) { event ->
                EventCard(event = event)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    EventGharTheme {
        DashboardScreen(navController = rememberNavController(), isDarkTheme = false, onThemeToggle = {})
    }
}