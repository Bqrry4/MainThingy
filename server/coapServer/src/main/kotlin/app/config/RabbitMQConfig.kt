package com.nyanthingy.app.config

import com.nyanthingy.app.AppContainer
import com.rabbitmq.client.*


object RabbitMQConfig {
    private const val EXCHANGE = "e.nyan"
    private const val QUEUE = "q.nyan"
    private const val ROUTING_KEY = "k.nyan"
    private const val RABBITMQ_HOST = "localhost"
    private const val RABBITMQ_PORT = 5672
    private const val RABBITMQ_USERNAME = "guest"
    private const val RABBITMQ_PASSWORD = "guest"

    fun createConnection(): Channel {
        val factory = ConnectionFactory()
        factory.host = RABBITMQ_HOST
        factory.port = RABBITMQ_PORT
        factory.username = RABBITMQ_USERNAME
        factory.password = RABBITMQ_PASSWORD

        val connection: Connection = factory.newConnection()
        val channel: Channel = connection.createChannel()

        channel.exchangeDeclare(EXCHANGE, "direct", true);
        channel.queueDeclare(QUEUE, true, false, false, null);
        channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);

        return channel
    }

    fun registerConsumerCallback(channel: Channel, deliverCallback: DeliverCallback) {
        channel.basicConsume(QUEUE, deliverCallback) { _ -> }
    }

}