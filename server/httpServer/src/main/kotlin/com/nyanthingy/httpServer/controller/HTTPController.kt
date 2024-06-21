package com.nyanthingy.httpServer.controller

import com.nyanthingy.httpServer.services.ValidationService
import com.nyanthingy.httpServer.dto.toDTO
import com.nyanthingy.httpServer.persistence.repository.GSPointRepository
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/device")
class HTTPController(
    private val _pointRepository: GSPointRepository,
    private val _validationService: ValidationService
) {
    @GetMapping("/{mac}")
    fun getLastPoints(
        @PathVariable(name = "mac", required = true) macAddr: String,
        @RequestParam("time_stamp")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") time_stamp: Date?,
        @RequestBody secret: String
    ): ResponseEntity<Any> {
        val deviceID = _validationService.validateClient(macAddr, secret)

        if(deviceID == -1) // no device found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (deviceID == -2) // invalid secret
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()

        time_stamp?.let { // all points for deviceID, newer than time_stamp
            return ResponseEntity.status(HttpStatus.OK)
                .body(_pointRepository.findByDate(deviceID, time_stamp).map { it.toDTO() })
        }

        // last point for deviceID
        return ResponseEntity.status(HttpStatus.OK).body(_pointRepository.findLast(deviceID).map { it.toDTO() })
    }
}