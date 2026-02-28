package com.example.eventghar.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventghar.data.StorageUtil
import com.example.eventghar.data.UserProfile
import com.example.eventghar.data.UserProfileDataStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private var profileListenerJob: kotlinx.coroutines.Job? = null

    init {
        // Start listening immediately if already logged in
        startListeningForCurrentUser()
        // Re-subscribe whenever auth state changes (e.g. after login)
        FirebaseAuth.getInstance().addAuthStateListener {
            startListeningForCurrentUser()
        }
    }

    private fun startListeningForCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            _userProfile.value = UserProfile()
            return
        }
        profileListenerJob?.cancel()
        profileListenerJob = viewModelScope.launch {
            UserProfileDataStore.userProfileFlow(uid).collect { profile ->
                _userProfile.value = profile
                Log.d("UserProfileVM", "Firestore realtime - name='${profile.name}' img='${profile.profileImageUri}'")
            }
        }
    }

    fun loadUserProfileFromFirestore(uid: String) {
        // Restart listener for the given uid — ensures correct profile loads after login
        profileListenerJob?.cancel()
        profileListenerJob = viewModelScope.launch {
            UserProfileDataStore.userProfileFlow(uid).collect { profile ->
                _userProfile.value = profile
                Log.d("UserProfileVM", "loadUserProfileFromFirestore uid=$uid name='${profile.name}'")
            }
        }
    }

    fun updateProfile(name: String, phone: String, profileImageUri: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            // Upload profile image to Firebase Storage if it's a local path
            val finalImageUri = uploadProfileImageIfNeeded(profileImageUri)
            val updated = _userProfile.value.copy(name = name, phone = phone, profileImageUri = finalImageUri)
            _userProfile.value = updated
            try {
                UserProfileDataStore.saveUserProfile(uid, updated)
            } catch (e: Exception) {
                Log.w("UserProfileVM", "Failed to save profile: ${e.message}")
            }
        }
    }

    fun updateProfileImage(imagePath: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("UserProfileVM", "Updating profile image: $imagePath")
        viewModelScope.launch {
            val finalUri = uploadProfileImageIfNeeded(imagePath)
            val updated = _userProfile.value.copy(profileImageUri = finalUri)
            _userProfile.value = updated
            try {
                UserProfileDataStore.saveUserProfile(uid, updated)
            } catch (e: Exception) {
                Log.w("UserProfileVM", "Failed to save profile image: ${e.message}")
            }
        }
    }

    fun updateProfileImage(imageUri: Uri) = updateProfileImage(imageUri.toString())

    private suspend fun uploadProfileImageIfNeeded(uri: String): String {
        if (uri.isBlank() || StorageUtil.isRemoteUrl(uri)) return uri
        // Local path — upload to Firebase Storage
        val downloadUrl = StorageUtil.uploadImage(
            getApplication(), "profile_images", uri
        )
        return downloadUrl ?: uri
    }
}
