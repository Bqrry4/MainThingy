package com.nyanthingy.mobileapp.modules.database.profile.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(profileEntry: ProfileEntry)
    @Update
    suspend fun update(profileEntry: ProfileEntry)
    @Delete
    suspend fun delete(profileEntry: ProfileEntry)
    @Query("SELECT * from profiles")
    fun getAll(): Flow<List<ProfileEntry>>
    @Query("SELECT macAddress from profiles")
    fun getMacAddresses() : Flow<List<String?>>
}