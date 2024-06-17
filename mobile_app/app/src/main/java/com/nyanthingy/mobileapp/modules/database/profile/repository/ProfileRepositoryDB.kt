package com.nyanthingy.mobileapp.modules.database.profile.repository

import com.nyanthingy.mobileapp.modules.database.profile.model.ProfileDao
import com.nyanthingy.mobileapp.modules.database.profile.model.fromDomain
import com.nyanthingy.mobileapp.modules.database.profile.model.toDomain
import com.nyanthingy.mobileapp.modules.profile.model.ProfileModel
import com.nyanthingy.mobileapp.modules.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of ProfileRepository with Room ORM
 */
class ProfileRepositoryDB @Inject constructor(
    private val _profileDao: ProfileDao
) : ProfileRepository {
    override fun getAll() = _profileDao.getAll().map { list ->
        list.map {
            it.toDomain()
        }
    }

    override suspend fun insert(profile: ProfileModel) =
        _profileDao.insert(profile.fromDomain())

    override suspend fun update(profile: ProfileModel) =
        _profileDao.update(profile.fromDomain())

    override suspend fun delete(profile: ProfileModel) =
        _profileDao.delete(profile.fromDomain())
}

