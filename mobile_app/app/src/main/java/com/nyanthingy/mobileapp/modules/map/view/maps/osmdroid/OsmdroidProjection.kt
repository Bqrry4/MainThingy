package com.nyanthingy.mobileapp.modules.map.view.maps.osmdroid

import android.graphics.Point
import com.nyanthingy.mobileapp.modules.map.view.maps.MapsProjection
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.Projection

class OsmdroidProjection(
    private val _projection: Projection
) : MapsProjection {
    override fun toPixels(geoPosition: GeoPosition): Point =
        _projection.toPixels(
            GeoPoint(
                geoPosition.latitude, geoPosition.longitude
            ),
            null
        ).let {
            //apply additional transformation for when map rotation is enabled
            _projection.rotateAndScalePoint(it.x, it.y, it)
        }

    override fun fromPixels(point: Point) =
        _projection.fromPixels(point.x, point.y)
            .let {
                GeoPosition(
                    it.latitude, it.longitude
                )
            }
}