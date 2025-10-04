package com.iv127.quizflow.core.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText

fun routingContextWebResponse(handler: suspend io.ktor.server.routing.RoutingContext.() -> WebResponse):
    suspend io.ktor.server.routing.RoutingContext.() -> Unit {
    return {
        val resp: WebResponse = this.handler()
        webResponse(call, { resp })
    }
}

suspend fun webResponse(call: ApplicationCall, handler: suspend () -> WebResponse) {
    val resp: WebResponse = handler()
    for ((name, values) in resp.headers())
        for (value in values)
            call.response.header(name, value)
    val statusCode =
        HttpStatusCode.fromValue(
            resp.statusCode,
        )
    when (resp) {
        is TextWebResponse -> {
            call.respondText(
                text = resp.body,
                status = statusCode,
            )
        }

        is JsonWebResponse -> {
            call.respond(
                KtorJsonWebResponse.create(
                    body = resp.body,
                    status = statusCode
                )
            )
        }
    }
}

