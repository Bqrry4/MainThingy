package com.nyanthingy.mobileapp.modules.map.virtualfences.model

import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
/**
 * Domain Model for VirtualFence
 */
data class VirtualFenceModel(
    val id: Int = 0,
    val name: String,
    val center: GeoPosition,
    val radius: Double
)