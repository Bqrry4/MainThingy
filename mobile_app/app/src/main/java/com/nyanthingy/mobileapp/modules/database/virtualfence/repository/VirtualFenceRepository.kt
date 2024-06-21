package com.nyanthingy.mobileapp.modules.database.virtualfence.repository

import com.nyanthingy.mobileapp.modules.database.virtualfence.model.VirtualFenceDao
import com.nyanthingy.mobileapp.modules.database.virtualfence.model.fromDomain
import com.nyanthingy.mobileapp.modules.database.virtualfence.model.toDomain
import com.nyanthingy.mobileapp.modules.map.virtualfences.model.VirtualFenceModel
import com.nyanthingy.mobileapp.modules.map.virtualfences.repository.VirtualFenceRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VirtualFenceRepositoryDB @Inject constructor(
    private val _virtualFenceDao: VirtualFenceDao
) : VirtualFenceRepository {
    override fun getAll() = _virtualFenceDao.getAll().map { list ->
        list.map {
            it.toDomain()
        }
    }

    override suspend fun insert(fence: VirtualFenceModel) =
        _virtualFenceDao.insert(
            fence.fromDomain()
        )

    override suspend fun update(fence: VirtualFenceModel) =
        _virtualFenceDao.update(
            fence.fromDomain()
        )

    override suspend fun delete(fence: VirtualFenceModel) =
        _virtualFenceDao.delete(
            fence.fromDomain()
        )

}