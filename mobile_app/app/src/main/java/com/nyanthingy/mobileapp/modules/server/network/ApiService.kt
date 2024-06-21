package com.nyanthingy.mobileapp.modules.server.network

import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import retrofit2.http.GET

interface ApiService {
    @GET("location")
    suspend fun getLocation(): List<GeoPosition>
}