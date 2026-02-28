package com.example.eventghar

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventghar.ui.admin.AdminDashboardScreen
import com.example.eventghar.ui.auth.ForgotPasswordScreen
import com.example.eventghar.ui.auth.LoginScreen
import com.example.eventghar.ui.auth.RegistrationScreen
import com.example.eventghar.ui.dashboard.DashboardScreen
import com.example.eventghar.ui.organizer.OrganizerDashboardScreen
import com.example.eventghar.ui.profile.EditProfileScreen
import com.example.eventghar.ui.user.UserDashboardScreen
import com.example.eventghar.ui.user.UserEventDetailHolder
import com.example.eventghar.ui.user.UserEventDetailScreen

@Composable
fun AppNavigation(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { 
            LoginScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            ) 
        }
        composable("registration") { 
            RegistrationScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            ) 
        }
        composable("forgot_password") { 
            ForgotPasswordScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            ) 
        }
        composable("dashboard") { 
            DashboardScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            ) 
        }
        composable("admin_dashboard") { 
            AdminDashboardScreen(navController = navController) 
        }
        composable("organizer_dashboard") { 
            OrganizerDashboardScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }
        composable("user_dashboard") {
            UserDashboardScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
        }
        composable("user_event_detail") {
            val event = UserEventDetailHolder.event
            if (event != null) {
                UserEventDetailScreen(event = event, navController = navController)
            }
        }
    }
}