package com.nyanthingy.mobileapp.modules.database.profile.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nyanthingy.mobileapp.modules.profile.model.ProfileModel

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
    val macAddress: String? = null,
    val secret: String? = null
)

fun ProfileEntry.toDomain() = ProfileModel(
    id = id,
    name = name,
    profileImageUri = profileImageUri,
    coverImageUri = coverImageUri,
    macAddress = macAddress
)

fun ProfileModel.fromDomain() = ProfileEntry(
    id = id,
    name = name,
    profileImageUri = profileImageUri,
    coverImageUri = coverImageUri,
    macAddress = macAddress
)