package com.nyanthingy.httpServer.rabbitmq

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component


@Component
class QueueWriterReader(private val amqpTemplate: AmqpTemplate) {
    fun sendMessage(msg: String){
        amqpTemplate.convertAndSend("e.nyan", "k.nyan", msg)
    }

//    @RabbitListener(queues = ["q.nyan"])
//    fun process(content: String?){
//        println("____________________"+content+"______________________")
//    }
}