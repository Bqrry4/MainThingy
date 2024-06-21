package com.nyanthingy.mobileapp.modules.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nyanthingy.mobileapp.modules.database.profile.model.ProfileDao
import com.nyanthingy.mobileapp.modules.database.profile.model.ProfileEntry
import com.nyanthingy.mobileapp.modules.database.virtualfence.model.VirtualFenceDao
import com.nyanthingy.mobileapp.modules.database.virtualfence.model.VirtualFenceEntry

@Database(
    entities = [ProfileEntry::class, VirtualFenceEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun virtualFenceDao(): VirtualFenceDao
}