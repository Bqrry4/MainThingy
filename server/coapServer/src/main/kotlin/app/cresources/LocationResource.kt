package app.cresources

import app.AppContainer
import app.dto.GeoEntryDTO
import app.persistance.model.Device
import app.persistance.model.GSPoint
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.coap.CoAP.*
import org.eclipse.californium.core.server.resources.CoapExchange
import java.util.*

class LocationResource : CoapResource("loc") {
    private val _mapper = AppContainer.mapper
    private val _gsPointRepository = AppContainer.gsPointRepository
    private val _deviceRepository = AppContainer.deviceRepository

    override fun handlePOST(exchange: CoapExchange) {

        runCatching {
            //find the parent resource first
            val device = _deviceRepository.findByMacAddress(parent.name)
                .also { result ->
                    if (result.none()) {
                        //Parent resource does not exist currently
                        exchange.respond(ResponseCode.NOT_FOUND)
                    }
                }.first()

            val entry: GeoEntryDTO = _mapper.reader().readValue(
                exchange.requestPayload,
                GeoEntryDTO::class.java
            )

            _gsPointRepository.insert(entry.toEntity(device))
            exchange.respond(ResponseCode.VALID)
        }.onFailure {
            when (it) {
                is StreamReadException,
                is UnrecognizedPropertyException -> exchange.respond(ResponseCode.BAD_REQUEST)

                else -> exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR)
            }
        }
    }
}

/**
 * @param device The device the entry corresponds
 */
fun GeoEntryDTO.toEntity(device: Device) = GSPoint(
    timestamp = Date(),
    longitude = lon,
    latitude = lat,
    accuracy = acr,
    device = device
)