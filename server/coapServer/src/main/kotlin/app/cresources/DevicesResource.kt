package app.cresources

import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.network.Exchange
import org.eclipse.californium.core.server.resources.Resource


/** Devices resource
 * @note No method for retrieving for now, as no simple user can interact with other devices
 */
class DevicesResource : CoapResource("devs") {

    /**
     * Handle the request for a child resource of the collection
     */
    override fun handleRequest(exchange: Exchange) {
        // Inspect the path to check for child resources
        val path = exchange.request.options.uriPath.toTypedArray()

        if (path.size == 1) {
            //The request is intended to this resource
            super.handleRequest(exchange)
            return
        }

        //The request is intended for the child resource
        val deviceId = path[1]

        val deviceResource: Resource =
            // Check if there is an instance in memory already
            getChild(deviceId) ?:
            // If not, create a new device resource and add it
            add(
                DeviceResource(deviceId)
            )
        // Delegate the request to child resource
        deviceResource.handleRequest(exchange)
    }
}