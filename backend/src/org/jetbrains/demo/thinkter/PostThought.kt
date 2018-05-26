package org.jetbrains.demo.thinkter

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.PostThoughtResult
import org.jetbrains.demo.thinkter.model.PostThoughtToken

fun Route.postThought(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<PostThought> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }

        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashFunction)
            call.respond(PostThoughtToken(user.userId, date, code))
        }
    }
    post<PostThought> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        if (user == null || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val id = dao.createThought(user.userId, it.text, it.replyTo)
            call.respond(PostThoughtResult(dao.getThought(id)))
        }
    }
}