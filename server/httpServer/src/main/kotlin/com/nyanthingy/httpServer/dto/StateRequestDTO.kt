package com.nyanthingy.httpServer.dto

data class StateRequestDTO (
    var secret: String,
    var state: Boolean
)

data class CoapStateDTO(
    var st: Boolean
)