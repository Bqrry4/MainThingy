package com.nyanthingy.mobileapp.modules.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nyanthingy.mobileapp.modules.database.model.ProfileDao
import com.nyanthingy.mobileapp.modules.database.model.ProfileEntry

@Database(entities = [ProfileEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}