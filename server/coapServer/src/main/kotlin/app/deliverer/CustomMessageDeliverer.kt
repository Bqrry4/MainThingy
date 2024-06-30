package com.nyanthingy.app.deliverer

import org.eclipse.californium.core.server.ServerMessageDeliverer
import org.eclipse.californium.core.server.resources.Resource
import org.eclipse.californium.elements.config.Configuration


class CustomMessageDeliverer(private val root: Resource, config: Configuration) : ServerMessageDeliverer(root, config) {

    override fun findResource(path: MutableList<String>?): Resource {
        //Return root when path is null
        var resource: Resource = root
        path ?: return resource

        //Iterate through path and give the last known resource in path
        for (name in path) {
            resource.getChild(name)?.also { resource = it } ?: break
        }

        return resource
    }
}