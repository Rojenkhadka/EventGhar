package com.example.eventghar

import com.example.eventghar.data.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileUnitTests {

    @Test
    fun userProfile_defaultValues_areCorrect() {
        val profile = UserProfile()
        assertEquals("", profile.name)
        assertEquals("", profile.email)
        assertTrue(profile.isVerified)
    }
}
