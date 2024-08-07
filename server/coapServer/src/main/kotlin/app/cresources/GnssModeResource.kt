package com.nyanthingy.app.cresources

import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.nyanthingy.app.AppContainer
import com.nyanthingy.app.dto.StateDto
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.coap.CoAP
import org.eclipse.californium.core.coap.CoAP.ResponseCode
import org.eclipse.californium.core.observe.ObserveRelation
import org.eclipse.californium.core.server.resources.CoapExchange

class GnssModeResource : CoapResource("gnssm"){
    init {
        isObservable = true;
        observeType = CoAP.Type.CON;
        attributes.setObservable();
    }

    private val _mapper = AppContainer.mapper

    //the state does not have to persist
    private var state: Boolean? = null

    //Only one device could observe its own resource
    override fun addObserveRelation(relation: ObserveRelation) {
        this.clearObserveRelations()
        super.addObserveRelation(relation)
    }

    override fun handleGET(exchange: CoapExchange) {
        //for observe purpose
        when (state) {
            null -> {
                exchange.respond(
                    ResponseCode.VALID
                )
            }

            else -> {
                exchange.respond(
                    ResponseCode.CONTENT,
                    _mapper.writeValueAsBytes(
                        StateDto(
                            st = state!!
                        )
                    )
                )
            }
        }
    }

    override fun handlePUT(exchange: CoapExchange) {

        runCatching {
            val dto = _mapper.reader().readValue(
                exchange.requestPayload,
                StateDto::class.java
            )
            state = dto.st

        }.onFailure {
            println(it.toString())
            when (it) {
                is StreamReadException,
                is UnrecognizedPropertyException -> exchange.respond(ResponseCode.BAD_REQUEST)

                else -> exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR)
            }
        }
//        changed()
//        notifyObserverRelations(null)
        exchange.respond(ResponseCode.CHANGED)
    }
}