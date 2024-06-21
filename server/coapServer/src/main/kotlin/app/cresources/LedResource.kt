package app.cresources

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.coap.CoAP.ResponseCode
import org.eclipse.californium.core.coap.CoAP.Type
import org.eclipse.californium.core.observe.ObserveRelation
import org.eclipse.californium.core.server.resources.CoapExchange

class LedResource : CoapResource("led"){
    init {
        isObservable = true;
        observeType = Type.CON;
        attributes.setObservable();
    }

    override fun addObserveRelation(relation: ObserveRelation?) {
        super.addObserveRelation(relation)
    }

    override fun handleGET(exchange: CoapExchange) {
//        exchange.respond("update")
        val deviceId = parent.name
        exchange.respond("LED Resource Observed by: ${this.observerCount}")

    }

    override fun handlePUT(exchange: CoapExchange) {
        exchange.respond(ResponseCode.CHANGED)
        changed() // notify all observers
    }
}