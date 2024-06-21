package app.persistance.repository.interfaces

import app.persistance.model.Device

interface DeviceRepository : Repository<Device, Int>{

    fun findByMacAddress(macAddress: String): Iterable<Device>
}