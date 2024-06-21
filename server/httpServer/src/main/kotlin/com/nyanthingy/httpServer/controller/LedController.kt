package com.nyanthingy.httpServer.controller

import com.fasterxml.jackson.databind.node.ObjectNode
import com.nyanthingy.httpServer.rabbitmq.QueueWriterReader
import com.nyanthingy.httpServer.services.ValidationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/device")
class LedController(
    private val _queue: QueueWriterReader,
    private val _validationService: ValidationService
){
    @PutMapping("/{mac}/led")
    fun setLedState(
        @PathVariable(name = "mac") mac: String,
        @RequestBody json: ObjectNode
    ): ResponseEntity<Any>{
        val secret = json.get("secret").asText()
        val state = json.get("state").asText()
        val deviceID = _validationService.validateClient(mac, secret)

        if(deviceID == -1) // no device found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (deviceID == -2) // invalid secret
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        _queue.sendMessage("led: $state")
        return ResponseEntity.status(HttpStatus.OK).build()
    }
}