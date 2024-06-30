package com.nyanthingy

import com.nyanthingy.app.NyanThingyCoapServer
import com.nyanthingy.app.config.CoapDtlsConfig

/* Application entry point */
fun main(args: Array<String>) {

    //apply configs
    CoapDtlsConfig().apply()

    //starting the server
    NyanThingyCoapServer().start()

}
