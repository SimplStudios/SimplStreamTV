package com.simplstudios.simplstream.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplstudios.simplstream.data.preferences.SessionManager
import com.simplstudios.simplstream.domain.model.Profile
import com.simplstudios.simplstream.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()
    
    init {
        loadProfiles()
        createDefaultProfileIfNeeded()
    }
    
    private fun createDefaultProfileIfNeeded() {
        viewModelScope.launch {
            try {
                // Check if any profiles exist, if not create a default one
                val profiles = profileRepository.getAllProfilesOnce()
                if (profiles.isEmpty()) {
                    profileRepository.createProfile(
                        name = "User",
                        avatarIndex = 0,
                        pin = null,
                        isKidsProfile = false
                    )
                }
            } catch (e: Exception) {
                // Ignore errors, profile will be created when user adds one
            }
        }
    }
    
    private fun loadProfiles() {
        viewModelScope.launch {
            profileRepository.getAllProfiles()
                .combine(sessionManager.currentProfileId) { profiles, currentId ->
                    _uiState.update { 
                        it.copy(
                            profiles = profiles,
                            currentProfileId = currentId,
                            isLoading = false
                        )
                    }
                }
                .collect()
        }
    }
    
    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            if (profile.hasPin) {
                _uiState.update { it.copy(pinDialogProfile = profile) }
            } else {
                setCurrentProfile(profile.id)
            }
        }
    }
    
    fun verifyPin(pin: String) {
        viewModelScope.launch {
            val profile = _uiState.value.pinDialogProfile ?: return@launch
            
            if (profileRepository.verifyPin(profile.id, pin)) {
                _uiState.update { it.copy(pinDialogProfile = null, pinError = null) }
                setCurrentProfile(profile.id)
            } else {
                _uiState.update { it.copy(pinError = "Incorrect PIN") }
            }
        }
    }
    
    fun verifyPinForAction(profileId: Long, pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (profileRepository.verifyPin(profileId, pin)) {
                onSuccess()
            } else {
                onError()
            }
        }
    }
    
    fun dismissPinDialog() {
        _uiState.update { it.copy(pinDialogProfile = null, pinError = null) }
    }
    
    private suspend fun setCurrentProfile(profileId: Long) {
        sessionManager.setCurrentProfile(profileId)
        _events.emit(ProfileEvent.ProfileSelected(profileId))
    }
    
    fun createProfile(
        name: String,
        avatarIndex: Int,
        pin: String?,
        isKidsProfile: Boolean
    ) {
        viewModelScope.launch {
            try {
                val id = profileRepository.createProfile(
                    name = name,
                    avatarIndex = avatarIndex,
                    pin = pin?.takeIf { it.length == 4 },
                    isKidsProfile = isKidsProfile
                )
                _events.emit(ProfileEvent.ProfileCreated(id))
            } catch (e: Exception) {
                _events.emit(ProfileEvent.Error(e.message ?: "Failed to create profile"))
            }
        }
    }
    
    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                profileRepository.updateProfile(profile)
                _events.emit(ProfileEvent.ProfileUpdated)
            } catch (e: Exception) {
                _events.emit(ProfileEvent.Error(e.message ?: "Failed to update profile"))
            }
        }
    }
    
    fun updateProfileDetails(
        profileId: Long,
        name: String,
        avatarIndex: Int,
        newPin: String?,
        isKidsProfile: Boolean,
        clearPin: Boolean
    ) {
        viewModelScope.launch {
            try {
                profileRepository.updateProfileDetails(
                    profileId = profileId,
                    name = name,
                    avatarIndex = avatarIndex,
                    newPin = newPin,
                    isKidsProfile = isKidsProfile,
                    clearPin = clearPin
                )
                _events.emit(ProfileEvent.ProfileUpdated)
            } catch (e: Exception) {
                _events.emit(ProfileEvent.Error(e.message ?: "Failed to update profile"))
            }
        }
    }

    fun deleteProfile(profile: Profile) {
        deleteProfile(profile.id)
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            try {
                profileRepository.deleteProfile(profileId)
                
                // If deleted profile was current, clear session
                if (_uiState.value.currentProfileId == profileId) {
                    sessionManager.clearCurrentProfile()
                }
                
                _events.emit(ProfileEvent.ProfileDeleted)
            } catch (e: Exception) {
                _events.emit(ProfileEvent.Error(e.message ?: "Failed to delete profile"))
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearCurrentProfile()
            _events.emit(ProfileEvent.LoggedOut)
        }
    }
    
    fun showAddProfileDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }
    
    fun hideAddProfileDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }
    
    fun setEditingProfile(profile: Profile?) {
        _uiState.update { it.copy(editingProfile = profile) }
    }
}

data class ProfileUiState(
    val profiles: List<Profile> = emptyList(),
    val currentProfileId: Long = SessionManager.NO_PROFILE,
    val isLoading: Boolean = true,
    val pinDialogProfile: Profile? = null,
    val pinError: String? = null,
    val showAddDialog: Boolean = false,
    val editingProfile: Profile? = null
) {
    val hasProfiles: Boolean get() = profiles.isNotEmpty()
    val currentProfile: Profile? get() = profiles.find { it.id == currentProfileId }
}

sealed class ProfileEvent {
    data class ProfileSelected(val profileId: Long) : ProfileEvent()
    data class ProfileCreated(val profileId: Long) : ProfileEvent()
    data object ProfileUpdated : ProfileEvent()
    data object ProfileDeleted : ProfileEvent()
    data object LoggedOut : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}
