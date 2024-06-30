package com.nyanthingy.app.persistance.repository.implementations

import com.nyanthingy.app.persistance.model.Device
import com.nyanthingy.app.persistance.repository.interfaces.DeviceRepository

class DeviceRepositoryImpl : DeviceRepository,
    JpaRepository<Device, Int>(Device::class.java) {

    override fun findByMacAddress(macAddress: String): Iterable<Device> {
        val jpql = "SELECT d FROM Device d WHERE d.macAddress = :macAddress"
        val query = entityManager.createQuery(jpql, Device::class.java)
        query.setParameter("macAddress", macAddress)
        return query.resultList
    }
}