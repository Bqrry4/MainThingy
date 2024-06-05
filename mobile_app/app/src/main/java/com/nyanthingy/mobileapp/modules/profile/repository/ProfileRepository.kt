package com.nyanthingy.mobileapp.modules.profile.repository

import com.nyanthingy.mobileapp.modules.database.model.ProfileDao
import com.nyanthingy.mobileapp.modules.database.model.ProfileEntry
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val _profileDao: ProfileDao
) {
    fun getAllProfiles() = _profileDao.getAll()

    suspend fun insertProfile(profile: ProfileEntry) = _profileDao.insert(profile)
    suspend fun updateProfile(profile: ProfileEntry) = _profileDao.update(profile)
    suspend fun deleteProfile(profile: ProfileEntry) = _profileDao.delete(profile)
}