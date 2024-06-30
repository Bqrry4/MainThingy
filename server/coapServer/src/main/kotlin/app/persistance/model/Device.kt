package com.nyanthingy.app.persistance.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Devices")
class Device(
    @Id var deviceID: Int = 0,
    @Column(name = "macAddress")
    var macAddress: String = "",
    @Column(name = "secret")
    var secret: String = ""
)