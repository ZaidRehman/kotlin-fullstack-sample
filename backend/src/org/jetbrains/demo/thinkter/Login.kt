package org.jetbrains.demo.thinkter

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.clear
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.LoginResponse

fun Route.login(dao: ThinkterStorage, hash: (String) -> String) {
    get<Login> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            call.respond(LoginResponse(user))
        }
    }
    post<Login> {
        val login = when {
            it.userId.length < 4 -> null
            it.password.length < 6 -> null
            !userNameValid(it.userId) -> null
            else -> dao.user(it.userId, hash(it.password))
        }

        if (login == null) {
            call.respond(LoginResponse(error = "Invalid username or password"))
        } else {
            //call.session(Session(login.userId))
            call.sessions.set(Session(login.userId))
            call.respond(LoginResponse(login))
        }
    }
    post<Logout> {
        call.sessions.clear<Session>()
        call.respond(HttpStatusCode.OK)
    }
}
