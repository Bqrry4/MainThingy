package com.nyanthingy.app.config

import org.eclipse.californium.core.config.CoapConfig
import org.eclipse.californium.elements.config.TcpConfig
import org.eclipse.californium.scandium.config.DtlsConfig

class CoapDtlsConfig {

    //set to default definitions
    fun apply()
    {
        CoapConfig.register()
        DtlsConfig.register()
        TcpConfig.register()
    }
}