package com.nyanthingy.mobileapp.modules.map.utils

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sqrt

//Earth radius on equator
const val earthEqRadius = 6378137

fun shiftLatitudeByMeters(latitude: Double, meters: Double) =
    latitude + (meters / earthEqRadius) * (180 / PI)

/**
 * Compute distance between two GeoPoints in meters.
 * @note Using Haversine formula https://en.wikipedia.org/wiki/Haversine_formula
 */
operator fun GeoPosition.minus(position: GeoPosition): Double {

    //convert latitudes to radians
    val phi1 = latitude * PI / 180
    val phi2 = position.latitude * PI / 180

    //compute differences in radians
    val dx = phi2 - phi1
    val dy = position.longitude * PI / 180 - longitude * PI / 180

    val haversine = (1 - cos(dx) + cos(phi1) * cos(phi2) * (1 - cos(dy))) / 2

    //distance
    return 2 * earthEqRadius * asin(sqrt(haversine))
}