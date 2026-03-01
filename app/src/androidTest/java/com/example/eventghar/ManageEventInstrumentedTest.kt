package com.example.eventghar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.eventghar.ui.organizer.Event
import com.example.eventghar.ui.organizer.OrganizerManageEventScreen
import org.junit.Rule
import org.junit.Test

class ManageEventInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun manageEventScreen_showsEventDetails() {
        val testEvent = Event(
            title = "Test Music Festival",
            category = "Concert",
            date = "02/26/2026",
            location = "Main Arena"
        )

        composeTestRule.setContent {
            OrganizerManageEventScreen(
                event = testEvent,
                onBack = {},
                onSave = {}
            )
        }

        composeTestRule.onNodeWithText("Edit Event Details").assertExists()
        composeTestRule.onNodeWithText("Test Music Festival").assertExists()
        composeTestRule.onNodeWithText("Concert").assertExists()
        composeTestRule.onNodeWithText("02/26/2026").assertExists()
        composeTestRule.onNodeWithText("Main Arena").assertExists()
    }

    @Test
    fun manageEventScreen_saveButton_exists() {
        composeTestRule.setContent {
            OrganizerManageEventScreen(
                event = Event(),
                onBack = {},
                onSave = {}
            )
        }

        composeTestRule.onNodeWithText("Save Changes").assertExists()
    }
}
