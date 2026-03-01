package com.example.eventghar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.eventghar.ui.organizer.Event
import com.example.eventghar.ui.organizer.OrganizerMyEventsScreen
import org.junit.Rule
import org.junit.Test

class EventInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun organizerMyEventsScreen_showsTitle() {
        composeTestRule.setContent {
            OrganizerMyEventsScreen(
                events = listOf(
                    Event(title = "Music Festival", date = "02/28/2026", status = "published")
                ),
                onManage = {},
                onDelete = {},
                onViewDetails = {},
                onPublish = {}
            )
        }

        composeTestRule.onNodeWithText("My Events").assertExists()
        composeTestRule.onNodeWithText("Music Festival").assertExists()
    }

    @Test
    fun organizerMyEventsScreen_emptyState_showsMessage() {
        composeTestRule.setContent {
            OrganizerMyEventsScreen(
                events = emptyList(),
                onManage = {},
                onDelete = {},
                onViewDetails = {},
                onPublish = {}
            )
        }

        composeTestRule.onNodeWithText("No events yet. Tap + to create your first event.").assertExists()
    }

    @Test
    fun organizerMyEventsScreen_searchFiltersEvents() {
        composeTestRule.setContent {
            OrganizerMyEventsScreen(
                events = listOf(
                    Event(title = "Music Festival", date = "02/28/2026", status = "published"),
                    Event(title = "Tech Workshop", date = "03/15/2026", status = "published")
                ),
                onManage = {},
                onDelete = {},
                onViewDetails = {},
                onPublish = {}
            )
        }

        composeTestRule.onNodeWithText("Search your events...").performTextInput("Tech")

        composeTestRule.onNodeWithText("Music Festival").assertDoesNotExist()
        composeTestRule.onNodeWithText("Tech Workshop").assertExists()
    }
}
