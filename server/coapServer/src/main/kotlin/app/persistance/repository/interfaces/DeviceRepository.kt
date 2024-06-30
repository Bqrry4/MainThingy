package com.nyanthingy.app.persistance.repository.interfaces

import com.nyanthingy.app.persistance.model.Device

interface DeviceRepository : Repository<Device, Int>{

    fun findByMacAddress(macAddress: String): Iterable<Device>
}