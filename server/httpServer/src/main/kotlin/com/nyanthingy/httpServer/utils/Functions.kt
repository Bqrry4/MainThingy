package com.nyanthingy.httpServer.utils

import org.eclipse.californium.core.coap.CoAP
import org.springframework.http.HttpStatus

/**
   Inspired from RFC 8075, section 7
 */
fun mapCoAPToHTTPCodes(code: CoAP.ResponseCode) = when(code)
{
    CoAP.ResponseCode.CREATED -> HttpStatus.CREATED
    CoAP.ResponseCode.DELETED -> HttpStatus.OK
    CoAP.ResponseCode.VALID -> HttpStatus.OK
    CoAP.ResponseCode.CHANGED -> HttpStatus.NO_CONTENT
    CoAP.ResponseCode.CONTENT -> HttpStatus.OK

    CoAP.ResponseCode.BAD_REQUEST -> HttpStatus.BAD_REQUEST
    CoAP.ResponseCode.UNAUTHORIZED -> HttpStatus.FORBIDDEN
    CoAP.ResponseCode.BAD_OPTION -> HttpStatus.BAD_REQUEST
    CoAP.ResponseCode.FORBIDDEN -> HttpStatus.FORBIDDEN
    CoAP.ResponseCode.NOT_FOUND -> HttpStatus.NOT_FOUND
    CoAP.ResponseCode.METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED

    else -> HttpStatus.INTERNAL_SERVER_ERROR
}