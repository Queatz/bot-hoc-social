package social.hoc.bot.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import social.hoc.bot.BotConfigField
import social.hoc.bot.BotDetails
import social.hoc.bot.Install
import social.hoc.bot.InstallBotBody
import social.hoc.bot.InstallBotResponse
import social.hoc.bot.MessageBotBody
import social.hoc.bot.MessageBotResponse
import social.hoc.bot.ReinstallBotBody
import social.hoc.bot.app
import kotlin.random.Random

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(
                BotDetails(
                    name = "Học tiếng Việt đi",
                    description = "A bot that gives you random Vietnamese words throughout the day.",
                    keywords = listOf("!"),
                    config = listOf(
                        BotConfigField(
                            key = "frequency",
                            label = "How many hours between words?",
                            type = "number"
                        )
                    )
                )
            )
        }

        post("/install") {
            val body = call.receive<InstallBotBody>()

            // todo secret

            val groupToken = (0..255).token()

            val install = Install(
                groupId = body.groupId,
                token = groupToken,
                webhook = body.webhook,
                config = body.config ?: emptyList()
            )

            app.install(install)

            call.respond(
                InstallBotResponse(
                    token = groupToken
                )
            )
        }

        post("/reinstall") {
            val body = call.receive<ReinstallBotBody>()
            val token = call.request.header(HttpHeaders.Authorization)?.split(" ")?.last()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            app.reinstall(token, body.config ?: emptyList())

            call.respond(
                call.respond(HttpStatusCode.NoContent)
            )
        }

        post("/uninstall") {
            val token = call.request.header(HttpHeaders.Authorization)?.split(" ")?.last()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            app.uninstall(token)

            call.respond(HttpStatusCode.NoContent)
        }

        post("/message") {
            val token = call.request.header(HttpHeaders.Authorization)?.split(" ")?.last()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val body = call.receive<MessageBotBody>()

            val response = app.message(token, body)

            call.respond(response)
        }
    }
}

fun IntRange.token() =
    joinToString("") {
        Random.nextInt(35).toString(36).let {
            if (Random.nextBoolean()) it.uppercase() else it
        }
    }
