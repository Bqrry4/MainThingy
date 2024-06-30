package com.nyanthingy.app

import com.nyanthingy.app.config.RabbitMQConfig
import com.nyanthingy.app.persistance.repository.implementations.DeviceRepositoryImpl
import com.nyanthingy.app.persistance.repository.implementations.GSPointRepositoryImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper

/**
 * Container used for dependency injection
 *
 * Using the singleton approach for now instead of the single instance,
 * as it's not possible to access the server instance across components
 */
object AppContainer {
    val deviceRepository = DeviceRepositoryImpl()
    val gsPointRepository = GSPointRepositoryImpl()

    val mapper: ObjectMapper = CBORMapper()
}