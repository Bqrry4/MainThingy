package com.nyanthingy.mobileapp.modules.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.profile.model.ProfileModel
import com.nyanthingy.mobileapp.modules.profile.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfilesState(val profileModelList: List<ProfileModel> = listOf())

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val _profileRepository: ProfileRepository
) : ViewModel() {

    val state =
        _profileRepository.getAll().map { ProfilesState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = ProfilesState()
            )

    var selectedProfile by mutableIntStateOf(0)
    suspend fun insert(profile: ProfileModel) = _profileRepository.insert(profile)
    suspend fun update(profile: ProfileModel) = _profileRepository.update(profile)
}