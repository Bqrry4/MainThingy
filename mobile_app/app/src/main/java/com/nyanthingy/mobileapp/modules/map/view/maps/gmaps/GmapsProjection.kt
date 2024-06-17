package com.nyanthingy.mobileapp.modules.map.view.maps.gmaps

import android.graphics.Point
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.LatLng
import com.nyanthingy.mobileapp.modules.map.view.maps.MapsProjection
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition

class GmapsProjection(
    private val _projection: Projection
) : MapsProjection {
    override fun toPixels(geoPosition: GeoPosition) =
        _projection.toScreenLocation(
            LatLng(
                geoPosition.latitude, geoPosition.longitude
            )
        )

    override fun fromPixels(point: Point) =
        _projection.fromScreenLocation(point).let {
            GeoPosition(
                it.latitude, it.longitude
            )
        }
}