package com.nyanthingy.mobileapp.modules.profile.model

/**
 * Domain Model for Profile
 */
data class ProfileModel(
    val id: Int = 0,
    val name: String,
    val profileImageUri: String,
    val coverImageUri: String? = null,
    val macAddress: String? = null
)