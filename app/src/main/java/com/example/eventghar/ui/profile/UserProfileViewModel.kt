package com.example.eventghar.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventghar.data.UserProfile
import com.example.eventghar.data.UserProfileDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = UserProfileDataStore(application)

    val userProfile: StateFlow<UserProfile> = dataStore.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    init {
        // Unconditional Firestore refresh on ViewModel creation so the
        // dashboard always shows the latest name / profile data.
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            loadUserProfileFromFirestore(uid)
        }
    }

    fun loadUserProfileFromFirestore(uid: String) {
        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val role = document.getString("role") ?: ""
                    viewModelScope.launch {
                        dataStore.saveUserProfile(
                            uid = uid,
                            name = name,
                            email = email,
                            phone = phone,
                            role = role
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("UserProfileViewModel", "Failed to load profile from Firestore", e)
            }
    }

    fun saveProfile(
        name: String,
        email: String,
        phone: String,
        profileImageUri: String = ""
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            dataStore.saveUserProfile(
                uid = uid,
                name = name,
                email = email,
                phone = phone,
                profileImageUri = profileImageUri
            )
        }
    }
}
