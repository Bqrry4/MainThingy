package com.nyanthingy.mobileapp.modules.map.virtualfences.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.map.virtualfences.model.VirtualFenceModel
import com.nyanthingy.mobileapp.modules.map.virtualfences.repository.VirtualFenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class VirtualFencesState(val virtualFencesList: List<VirtualFenceModel> = listOf())

//for polymorphic way https://stackoverflow.com/questions/51972843/polymorphic-entities-in-room
@HiltViewModel
class VirtualFencesViewModel @Inject constructor(
    private val _virtualFencesRepository: VirtualFenceRepository
): ViewModel() {

    val state =
        _virtualFencesRepository.getAll().map { VirtualFencesState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = VirtualFencesState()
            )

    suspend fun insert(virtualFence: VirtualFenceModel) = _virtualFencesRepository.insert(virtualFence)
    suspend fun update(virtualFence: VirtualFenceModel) = _virtualFencesRepository.update(virtualFence)
}