package com.nyanthingy.mobileapp.modules.server.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Date

interface ApiService {
    @GET("devices/{mac_address}/location/last")
    suspend fun getLocation(
        @Path("mac_address") macAddress : String,
        @Header("Authorization") authHeader: String
    ): GSPointResponseDTO

    /**
     * Y-M-D h:m:s.123
     */
    @GET("devices/{mac_address}/location")
    suspend fun getLocationsByTimestamp(
        @Path("mac_address") macAddress : String,
        @Header("Authorization") authHeader: String,
        @Query("time_stamp") timestamp: String
    ): Iterable<GSPointResponseDTO>

    @PUT("proxy/coap/devs/{mac_address}/led")
    suspend fun setLedState(
        @Path("mac_address") macAddress : String,
        @Header("Authorization") authHeader: String,
        @Body state: StateRequestDTO
    )

    @PUT("proxy/coap/devs/{mac_address}/buzz")
    suspend fun setBuzzerState(
        @Path("mac_address") macAddress : String,
        @Header("Authorization") authHeader: String,
        @Body state: StateRequestDTO
        )

    @PUT("proxy/coap/devs/{mac_address}/gnssm")
    suspend fun setGNSSModeState(
        @Path("mac_address") macAddress : String,
        @Header("Authorization") authHeader: String,
        @Body state: StateRequestDTO
    )

}

data class StateRequestDTO (
    var state: Boolean
)

data class GSPointResponseDTO (
    var timestamp: Date,
    var longitude: Float,
    var latitude: Float,
    var accuracy: Float
)
