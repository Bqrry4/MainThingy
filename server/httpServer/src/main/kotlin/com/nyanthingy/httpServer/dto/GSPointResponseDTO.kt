package com.nyanthingy.httpServer.dto

import com.nyanthingy.httpServer.persistence.model.GSPoint
import java.util.*


data class GSPointResponseDTO (
    var timestamp: Date,
    var longitude: Float,
    var latitude: Float,
    var accuracy: Float
)

fun GSPoint.toDTO() = GSPointResponseDTO(
    timestamp = timestamp,
    longitude = longitude,
    latitude = latitude,
    accuracy = accuracy
)