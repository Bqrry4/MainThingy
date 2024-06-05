package com.nyanthingy.mobileapp.modules.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profiles",
    indices = [Index(value = ["macAddress"], unique = true)]
)
data class ProfileEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val profileImageUri: String,
    val coverImageUri: String? = null,
    val macAddress: String? = null
)