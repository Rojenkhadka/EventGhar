package com.example.eventghar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.eventghar.data.UserProfile
import com.example.eventghar.ui.organizer.OrganizerSettingsScreen
import org.junit.Rule
import org.junit.Test

class SettingsInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysUserProfileInfo() {
        val testProfile = UserProfile(
            name = "Organizer Alex",
            email = "alex@eventghar.com",
            isVerified = true
        )

        composeTestRule.setContent {
            OrganizerSettingsScreen(
                navController = rememberNavController(),
                userProfile = testProfile
            )
        }

        // Verify profile details
        composeTestRule.onNodeWithText("Organizer Alex").assertExists()
        composeTestRule.onNodeWithText("alex@eventghar.com").assertExists()
        composeTestRule.onNodeWithText("Verified Organizer").assertExists()
    }

    @Test
    fun settingsScreen_showsLogoutDialog() {
        composeTestRule.setContent {
            OrganizerSettingsScreen(
                navController = rememberNavController(),
                userProfile = UserProfile(name = "Test User")
            )
        }

        // Click logout button
        composeTestRule.onNodeWithText("Logout").performClick()

        // Verify dialog appears
        composeTestRule.onNodeWithText("Are you sure you want to log out?").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
    }

    @Test
    fun settingsScreen_containsAccountManagementItems() {
        composeTestRule.setContent {
            OrganizerSettingsScreen(
                navController = rememberNavController(),
                userProfile = UserProfile()
            )
        }

        // Check for specific menu items
        composeTestRule.onNodeWithText("ACCOUNT MANAGEMENT").assertExists()
        composeTestRule.onNodeWithText("Edit Profile").assertExists()
        composeTestRule.onNodeWithText("Change Password").assertExists()
        composeTestRule.onNodeWithText("Dark Mode").assertExists()
    }
}
