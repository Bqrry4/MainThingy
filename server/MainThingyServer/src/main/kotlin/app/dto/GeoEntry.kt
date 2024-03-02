package app.dto

data class GeoEntry(
    /*longitude*/
    var lon: Float = 0f,
    /*latitude*/
    var lat: Float = 0f,
    /*accuracy*/
    var acr: Float = 0f
)