package com.nyanthingy.httpServer.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.nyanthingy.httpServer.config.CoApClientConfig
import com.nyanthingy.httpServer.dto.CoapStateDTO
import com.nyanthingy.httpServer.dto.StateRequestDTO
import com.nyanthingy.httpServer.services.ValidationService
import com.nyanthingy.httpServer.utils.mapCoAPToHTTPCodes
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.CoAP
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/proxy/coap/devs")
class HTTPToCoAPController(
    private val _validationService: ValidationService,
    private val _coapClient: CoapClient
) {
    private val _mapper: ObjectMapper = CBORMapper()

    @PutMapping("/{mac}/led")
    fun setLedState(
        @PathVariable(name = "mac") mac: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) secret: String,
        @RequestBody req: StateRequestDTO
    ): ResponseEntity<Any> {
        val deviceID = _validationService.validateClient(mac, secret)

        if (deviceID == -1) // no device found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (deviceID == -2) // bad header format
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

        if (deviceID == -3) // invalid secret
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        _coapClient.uri = CoApClientConfig.defaultUri + "/devs/$mac/led"

        val dtoCbor = _mapper.writeValueAsBytes(
            CoapStateDTO(req.state)
        )

        val response = _coapClient.put(
            dtoCbor,
            MediaTypeRegistry.APPLICATION_CBOR
        )

        return if (response.code == CoAP.ResponseCode.CONTENT)
            ResponseEntity
                .status(mapCoAPToHTTPCodes(response.code))
                .body(response.payload)
        else
            ResponseEntity
                .status(mapCoAPToHTTPCodes(response.code))
                .build()
    }

    @PutMapping("/{mac}/buz")
    fun setBuzzState(
        @PathVariable(name = "mac") mac: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) secret: String,
        @RequestBody req: StateRequestDTO
    ): ResponseEntity<Any> {
        val deviceID = _validationService.validateClient(mac, secret)

        if (deviceID == -1) // no device found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (deviceID == -2) // bad header format
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

        if (deviceID == -3) // invalid secret
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        _coapClient.uri = CoApClientConfig.defaultUri + "/devs/$mac/buz"

        val dtoCbor = _mapper.writeValueAsBytes(
            CoapStateDTO(req.state)
        )

        val response = _coapClient.put(
            dtoCbor,
            MediaTypeRegistry.APPLICATION_CBOR
        )

        if (response.code == CoAP.ResponseCode.CONTENT)
            return ResponseEntity
                .status(mapCoAPToHTTPCodes(response.code))
                .body(response.payload)
        else
            return ResponseEntity
                .status(mapCoAPToHTTPCodes(response.code))
                .build()
    }

    @PutMapping("/{mac}/gnssm")
    fun setGnssMode(
        @PathVariable(name = "mac") mac: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) secret: String,
        @RequestBody req: StateRequestDTO
    ): ResponseEntity<Any> {
        val deviceID = _validationService.validateClient(mac, secret)

        if (deviceID == -1) // no device found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (deviceID == -2) // bad header format
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()

        if (deviceID == -3) // invalid secret
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        _coapClient.uri = CoApClientConfig.defaultUri + "/devs/$mac/gnssm"

        val dtoCbor = _mapper.writeValueAsBytes(
            CoapStateDTO(req.state)
        )

        val response = _coapClient.put(
            dtoCbor,
            MediaTypeRegistry.APPLICATION_CBOR
        )

        if (response.code == CoAP.ResponseCode.CONTENT)
            return ResponseEntity
                .status(mapCoAPToHTTPCodes(response.code))
                .body(response.payload)
        else
            return ResponseEntity
                .status(mapCoAPToHTTPCodes(response.code))
                .build()
    }
}