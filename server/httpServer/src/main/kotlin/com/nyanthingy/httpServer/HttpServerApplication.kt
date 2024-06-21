package com.nyanthingy.httpServer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HttpServerApplication

fun main(args: Array<String>) {
	runApplication<HttpServerApplication>(*args)
}
