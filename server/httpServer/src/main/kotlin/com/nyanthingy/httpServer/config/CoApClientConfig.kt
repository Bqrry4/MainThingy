package com.nyanthingy.httpServer.config

import jakarta.annotation.PostConstruct
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.config.CoapConfig
import org.eclipse.californium.elements.config.UdpConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class CoApClientConfig {

    companion object {
        @Value("\${coap.client.timeout}")
        private val timeout = 0

        @Value("\${coap.client.uri}")
        var defaultUri: String = "coap://localhost:5683"
    }

    init {
        CoapConfig.register()
        UdpConfig.register()
    }

    @Bean
    fun coapClient(): CoapClient {
        return CoapClient()
    }
}

