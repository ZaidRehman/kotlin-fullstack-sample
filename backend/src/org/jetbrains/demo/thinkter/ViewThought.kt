package org.jetbrains.demo.thinkter

import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.ViewThoughtResponse

fun Route.viewThought(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<ViewThought> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        val date = System.currentTimeMillis()
        val code = if (user != null) call.securityCode(date, user, hashFunction) else null

        call.respond(ViewThoughtResponse(dao.getThought(it.id), date, code))
    }
}

