package com.nyanthingy.mobileapp.modules.profile.repository

import com.nyanthingy.mobileapp.modules.profile.model.ProfileModel
import kotlinx.coroutines.flow.Flow

interface ProfileRepository
{
    fun getAll(): Flow<List<ProfileModel>>
    suspend fun insert(profile: ProfileModel)
    suspend fun update(profile: ProfileModel)
    suspend fun delete(profile: ProfileModel)
}