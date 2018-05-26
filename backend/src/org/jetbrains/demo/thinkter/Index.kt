package org.jetbrains.demo.thinkter

import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.response.ApplicationSendPipeline
import io.ktor.response.etag
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.IndexResponse
import org.jetbrains.demo.thinkter.model.PollResponse
import org.jetbrains.demo.thinkter.model.Thought
import java.time.LocalDateTime
import java.time.ZoneId


fun Route.index(storage: ThinkterStorage) {
        get<Index> {
            val user = call.sessions.get<Session>()?.let { storage.user(it.userId) }
            val top = storage.top(10).map(storage::getThought)
            val latest = storage.latest(10).map(storage::getThought)

            call.response.pipeline.intercept(ApplicationSendPipeline.After) {
                val etagString = user?.userId + "," + top.joinToString { it.id.toString() } + latest.joinToString { it.id.toString() }
                call.response.etag(etagString)
            }

            call.respond(IndexResponse(top, latest))
        }
        get<Poll> { poll ->
            if (poll.lastTime.isBlank()) {
                call.respond(PollResponse(System.currentTimeMillis(), "0"))
            } else {
                val time = System.currentTimeMillis()
                val lastTime = poll.lastTime.toLong()

                val count = storage.latest(10).reversed().takeWhile { storage.getThought(it).toEpochMilli() > lastTime }.size

                call.respond(PollResponse(time, if (count == 10) "10+" else count.toString()))
            }
        }


}

private fun Thought.toEpochMilli() = LocalDateTime.parse(date).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
