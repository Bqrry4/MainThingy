package com.nyanthingy.httpServer.persistence.repository

import com.nyanthingy.httpServer.persistence.model.Device
import com.nyanthingy.httpServer.persistence.model.GSPoint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Date

interface DeviceRepository : JpaRepository<Device, Int> {
    fun findByMacAddress(mac: String): Iterable<Device>
}

interface GSPointRepository : JpaRepository<GSPoint, Date>{
    @Query(value = "select * from GSPoints where deviceID = ?1 order by time_stamp desc limit 1",
        nativeQuery = true)
    fun findLast(deviceID: Int): Iterable<GSPoint>

    @Query(value = "select * from GSPoints where deviceID = ?1 and time_stamp > (\"?2\") order by time_stamp desc",
        nativeQuery = true)
    fun findByDate(deviceID: Int, time: Date): Iterable<GSPoint>

    @Query(value = "select * from GSPoints where deviceID = ?1", nativeQuery = true)
    fun findByDeviceID(deviceID: Int): Iterable<GSPoint>
}