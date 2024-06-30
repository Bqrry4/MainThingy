package com.nyanthingy.app.persistance.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "GSPoints")
class GSPoint (
    @Column(name = "time_stamp")
    @Id var timestamp: Date,
    @Column(name = "longitude")
    var longitude: Float,
    @Column(name = "latitude")
    var latitude: Float,
    @Column(name = "accuracy")
    var accuracy: Float,
    @ManyToOne
    @JoinColumn(name = "deviceID")
    var device: Device
)