package app.cresources

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.coap.CoAP
import org.eclipse.californium.core.server.resources.CoapExchange

class BuzzerResource : CoapResource("buz"){
    init {
        isObservable = true;
        observeType = CoAP.Type.CON;
        attributes.setObservable();
    }

    override fun handleGET(exchange: CoapExchange) {
        exchange.setMaxAge(1) // the Max-Age value should match the update interval
        exchange.respond("update")
    }

    override fun handlePUT(exchange: CoapExchange) {
        exchange.respond("CHANGED")
        changed() // notify all observers
    }
}