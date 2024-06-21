package com.nyanthingy.httpServer.services

import com.nyanthingy.httpServer.persistence.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class ValidationService (
    private val _deviceRepository: DeviceRepository
){
    fun validateClient(mac: String, secret: String): Int {
        val devices = _deviceRepository.findByMacAddress(mac)

        if(devices.count() == 0)
            return -1

        val device = devices.first()
        if(device.secret != secret)
            return -2

        return device.deviceID
    }
}