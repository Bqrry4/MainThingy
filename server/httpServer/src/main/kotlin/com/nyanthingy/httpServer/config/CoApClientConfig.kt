package com.nyanthingy.httpServer.config

import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.config.CoapConfig
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.elements.config.TcpConfig
import org.eclipse.californium.elements.config.UdpConfig
import org.eclipse.californium.elements.tcp.netty.TcpClientConnector
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File


@Configuration
class CoApClientConfig {

    companion object {
        @Value("\${coap.client.timeout}")
        private val timeout = 0

        @Value("\${coap.client.uri}")
        var defaultUri: String = "coap://coap-service:5683"
    }

    init {
        CoapConfig.register()
        UdpConfig.register()
//        TcpConfig.register()
    }

    @Bean
    fun coapClient(): CoapClient {
        // config
//        val config = org.eclipse.californium.elements.config.Configuration.getStandard()
//
//        // Create TCP client connector
//        val tcpConnector = TcpClientConnector(config)
//
//
//        // Create CoAP endpoint with TCP connector
//        val builder = CoapEndpoint.Builder()
//        builder.setConnector(tcpConnector)
//        val endpoint = builder.build()
//        val client = CoapClient()
//        client.setEndpoint(endpoint)
        return CoapClient()
    }
}

