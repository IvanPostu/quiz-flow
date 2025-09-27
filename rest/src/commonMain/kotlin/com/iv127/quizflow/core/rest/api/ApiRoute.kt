package com.iv127.quizflow.core.rest.api

import io.ktor.server.routing.Route

interface ApiRoute {

    fun setup(parent: Route);

}
