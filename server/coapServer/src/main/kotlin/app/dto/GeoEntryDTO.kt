package com.nyanthingy.app.dto

data class GeoEntryDTO(
    /*longitude*/
    var lon: Float = 0f,
    /*latitude*/
    var lat: Float = 0f,
    /*accuracy*/
    var acr: Float = 0f
)