package com.example.eventghar.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore-backed repository for user profiles.
 * Collection: "users"   Document ID = Firebase Auth UID
 */
object UserProfileDataStore {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("users")

    private fun currentUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    /** Real-time Flow of the current user's profile. */
    fun userProfileFlow(): Flow<UserProfile> = userProfileFlow(currentUid())

    /** Real-time Flow of a specific user's profile. */
    fun userProfileFlow(uid: String): Flow<UserProfile> = callbackFlow {
        val listener: ListenerRegistration = collection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(UserProfile())
                    return@addSnapshotListener
                }
                val name = snapshot.getString("name") ?: ""
                val email = snapshot.getString("email") ?: ""
                val phone = snapshot.getString("phone") ?: ""
                val profileImageUri = snapshot.getString("profileImageUri") ?: ""
                val role = snapshot.getString("role") ?: ""
                trySend(
                    UserProfile(
                        name = name,
                        email = email,
                        phone = phone,
                        profileImageUri = profileImageUri,
                        isVerified = role == "Organizer"
                    )
                )
            }
        awaitClose { listener.remove() }
    }

    /** Save / merge a user profile to Firestore. */
    suspend fun saveUserProfile(uid: String, profile: UserProfile) {
        val data = mapOf(
            "name" to profile.name,
            "email" to profile.email,
            "phone" to profile.phone,
            "profileImageUri" to profile.profileImageUri,
            // Don't overwrite "role" — it's set during registration
        )
        collection.document(uid).set(data, SetOptions.merge()).await()
    }

    // ── Legacy overloads — context parameter ignored ──

    @Suppress("UNUSED_PARAMETER")
    fun userProfileFlow(context: Any, uid: String): Flow<UserProfile> = userProfileFlow(uid)

    @Suppress("UNUSED_PARAMETER")
    suspend fun saveUserProfile(context: Any, profile: UserProfile) =
        saveUserProfile(currentUid(), profile)

    @Suppress("UNUSED_PARAMETER")
    suspend fun saveUserProfile(context: Any, uid: String, profile: UserProfile) =
        saveUserProfile(uid, profile)
}
