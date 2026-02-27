package com.example.eventghar.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userProfileDataStore by preferencesDataStore(name = "user_profile")

class UserProfileDataStore(private val context: Context) {

    private fun currentUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    private fun nameKey(uid: String) = stringPreferencesKey("name_$uid")
    private fun emailKey(uid: String) = stringPreferencesKey("email_$uid")
    private fun phoneKey(uid: String) = stringPreferencesKey("phone_$uid")
    private fun roleKey(uid: String) = stringPreferencesKey("role_$uid")
    private fun imageKey(uid: String) = stringPreferencesKey("image_$uid")

    val userProfileFlow: Flow<UserProfile>
        get() {
            val uid = currentUid()
            return context.userProfileDataStore.data.map { prefs ->
                UserProfile(
                    name = prefs[nameKey(uid)] ?: "",
                    email = prefs[emailKey(uid)] ?: "",
                    phone = prefs[phoneKey(uid)] ?: "",
                    role = prefs[roleKey(uid)] ?: "",
                    profileImageUri = prefs[imageKey(uid)] ?: ""
                )
            }
        }

    suspend fun saveUserProfile(
        uid: String = currentUid(),
        name: String = "",
        email: String = "",
        phone: String = "",
        role: String = "",
        profileImageUri: String = ""
    ) {
        context.userProfileDataStore.edit { prefs ->
            prefs[nameKey(uid)] = name
            prefs[emailKey(uid)] = email
            prefs[phoneKey(uid)] = phone
            prefs[roleKey(uid)] = role
            prefs[imageKey(uid)] = profileImageUri
        }
    }
}
