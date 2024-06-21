package com.nyanthingy.httpServer.controller

import com.fasterxml.jackson.databind.node.ObjectNode
import com.nyanthingy.httpServer.rabbitmq.QueueWriterReader
import com.nyanthingy.httpServer.services.ValidationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/device")
class BuzzerController(
    private val _queue: QueueWriterReader,
    private val _validationService: ValidationService
) {
    @PutMapping("/{mac}/buzz")
    fun setBuzzState(
        @PathVariable(name = "mac") mac: String,
        @RequestBody json: ObjectNode
    ): ResponseEntity<Any> {
        val secret = json.get("secret").asText()
        val state = json.get("state").asText()
        val deviceID = _validationService.validateClient(mac, secret)

        if(deviceID == -1) // no device found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (deviceID == -2) // invalid secret
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        _queue.sendMessage("buzz: $state")
        return ResponseEntity.status(HttpStatus.OK).build()
    }
}