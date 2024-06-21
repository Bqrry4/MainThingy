package com.nyanthingy.mobileapp.modules.map.virtualfences.repository

import com.nyanthingy.mobileapp.modules.map.virtualfences.model.VirtualFenceModel
import kotlinx.coroutines.flow.Flow

interface VirtualFenceRepository
{
    fun getAll(): Flow<List<VirtualFenceModel>>
    suspend fun insert(fence: VirtualFenceModel)
    suspend fun update(fence: VirtualFenceModel)
    suspend fun delete(fence: VirtualFenceModel)
}