package com.nyanthingy.mobileapp.modules.database.virtualfence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.virtualfences.model.VirtualFenceModel

@Entity(
    tableName = "virtual_fences"
)
data class VirtualFenceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radius: Double
)

fun VirtualFenceEntry.toDomain() = VirtualFenceModel(
    id = id,
    name = name,
    center = GeoPosition(
        latitude = centerLatitude,
        longitude = centerLongitude
    ),
    radius = radius
)

fun VirtualFenceModel.fromDomain() = VirtualFenceEntry(
    id = id,
    name = name,
    centerLatitude = center.latitude,
    centerLongitude = center.longitude,
    radius = radius
)