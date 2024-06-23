package com.nyanthingy.mobileapp.modules.ble.client.repository

import com.nyanthingy.mobileapp.modules.profile.model.ProfileModel
import kotlinx.coroutines.flow.Flow

interface DevicesRepository
{
    fun getAll(): Flow<List<String>>
}