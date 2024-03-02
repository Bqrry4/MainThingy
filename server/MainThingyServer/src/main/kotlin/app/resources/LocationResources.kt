package app.resources

import app.dto.GeoEntry
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.coap.CoAP
import org.eclipse.californium.core.server.resources.CoapExchange

class LocationResource : CoapResource("loc") {
    private val mapper: ObjectMapper = CBORMapper()

    override fun handlePOST(exchange: CoapExchange) {
        try {
            val entry: GeoEntry = mapper.reader().readValue(
                exchange.requestPayload,
                GeoEntry::class.java
            )

            println(entry.lat)
            println(entry.lon)
            println(entry.acr)

            exchange.respond(CoAP.ResponseCode.VALID)
        } catch (e: StreamReadException) {
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST)
        } catch (e: UnrecognizedPropertyException) {
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR)
        }
    }
}