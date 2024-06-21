package com.nyanthingy.mobileapp.modules.map.view.maps

import android.graphics.Point
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition

interface MapsProjection {
    fun toPixels(geoPosition: GeoPosition): Point
    fun fromPixels(point: Point): GeoPosition
}