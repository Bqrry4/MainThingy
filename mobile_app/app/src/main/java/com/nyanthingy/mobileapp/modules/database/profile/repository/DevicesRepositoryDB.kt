package com.nyanthingy.mobileapp.modules.database.profile.repository

import com.nyanthingy.mobileapp.modules.ble.client.repository.DevicesRepository
import com.nyanthingy.mobileapp.modules.database.profile.model.ProfileDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of DevicesRepository with Room ORM
 */
class DevicesRepositoryDB @Inject constructor(
    private val _profileDao: ProfileDao
) :DevicesRepository {
    override fun getAll(): Flow<List<String>> = _profileDao.getMacAddresses().map { it.filterNotNull() }
}