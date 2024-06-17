package com.nyanthingy.mobileapp.modules.database.virtualfence.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VirtualFenceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(virtualFenceEntry: VirtualFenceEntry)
    @Update
    suspend fun update(virtualFenceEntry: VirtualFenceEntry)
    @Delete
    suspend fun delete(virtualFenceEntry: VirtualFenceEntry)
    @Query("SELECT * from virtual_fences")
    fun getAll(): Flow<List<VirtualFenceEntry>>
}