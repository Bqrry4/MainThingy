package com.nyanthingy.mobileapp.modules.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.database.model.ProfileEntry
import com.nyanthingy.mobileapp.modules.profile.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val _profileRepository: ProfileRepository
) : ViewModel() {

    val state =
        _profileRepository.getAllProfiles().map { ProfilesState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = ProfilesState()
            )

    var selectedProfile by mutableIntStateOf(0)
    suspend fun insert(profile: ProfileEntry) = _profileRepository.insertProfile(profile)


}

data class ProfilesState(val profileList: List<ProfileEntry> = listOf())