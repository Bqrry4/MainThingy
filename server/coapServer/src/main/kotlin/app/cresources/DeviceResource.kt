package app.cresources

import app.AppContainer
import app.dto.GeoEntryDTO
import app.dto.SecretEntryDTO
import app.persistance.model.Device
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.coap.CoAP.ResponseCode
import org.eclipse.californium.core.network.Exchange
import org.eclipse.californium.core.server.resources.CoapExchange


class DeviceResource(macAddress: String) : CoapResource(macAddress) {
    private val _mapper = AppContainer.mapper
    private val _deviceRepository = AppContainer.deviceRepository

    init {
        //Add child resources
        add(
            LocationResource(),
            LedResource(),
            BuzzerResource(),
            GnssModeResource()
        )
    }

    override fun handlePUT(exchange: CoapExchange) {

        runCatching {
            //deserialize first
            val entry: SecretEntryDTO = _mapper.reader().readValue(
                exchange.requestPayload,
                SecretEntryDTO::class.java
            )

            val device = _deviceRepository.findByMacAddress(this.name)
                .also { result ->
                    //if it does not exist
                    if (result.none()) {
                        //create resource
                        _deviceRepository.insert(
                            Device(
                                macAddress = this.name,
                                secret = entry.sec
                            )
                        )

                        exchange.respond(ResponseCode.CREATED)
                    }
                }.first()

            //update resource
            _deviceRepository.update(
                Device(
                    deviceID = device.deviceID,
                    macAddress = device.macAddress,
                    secret = entry.sec
                )
            )

            exchange.respond(ResponseCode.CHANGED)
        }.onFailure {
            when (it) {
                is StreamReadException,
                is UnrecognizedPropertyException -> exchange.respond(ResponseCode.BAD_REQUEST)

                else -> exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR)
            }
        }
    }

    /**
     * @note There is a little bug, when accessing the child of this resource instead of the resource itself
     * it won't go to the child resource.
     * ex. /devs/ff:ff/led, will execute as /devs/ff:ff on the first call
     */
    override fun handleRequest(exchange: Exchange) {

        //Temporary solution
        val uriPath = exchange.request.options.uriPath
        val indexOfRes = uriPath.indexOf(this.name)
        //if this resource is last in path
        if (indexOfRes == uriPath.count() - 1) {
            super.handleRequest(exchange)
            return
        }

        //else try to delegate to the child
        this.getChild(uriPath[indexOfRes + 1])?.also { child ->
            child.handleRequest(exchange)
        } ?: super.handleRequest(exchange)
    }
}