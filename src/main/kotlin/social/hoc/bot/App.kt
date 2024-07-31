package social.hoc.bot

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import social.hoc.bot.plugins.json
import java.awt.SystemColor.text
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlin.random.Random.Default.nextBoolean
import kotlin.text.Charsets.UTF_8
import kotlin.time.Duration.Companion.minutes

@Serializable
data class Install(
    val groupId: String,
    val token: String,
    val webhook: String,
    val config: List<BotConfigValue>
)

@Serializable
data class AppState(
    val delayInHours: Int = 4,
    val installs: List<Install> = emptyList()
)

val http = HttpClient(CIO) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(json)
    }

    engine {
        requestTimeout = 2.minutes.inWholeMilliseconds
    }
}

class App {

    private val stateFile = File("./state.json")
    private var state: AppState
    private val scope = CoroutineScope(Dispatchers.Default)
    private val jobs = mutableListOf<Pair<String, Job>>()

    init {
        state = if (stateFile.exists()) stateFile.readText().let {
            json.decodeFromString(it)
        } else {
            AppState()
        }
    }

    fun start() {
        scope.launch {
            state.installs.forEach {
                startInstall(it)
            }
        }
    }

    fun stop() {
        jobs.forEach {
            it.second.cancel()
        }
        scope.cancel()
    }

    suspend fun message(token: String, message: MessageBotBody): MessageBotResponse {
        return MessageBotResponse(
            success = true,
            note = "Được luôn",
            actions = listOf(
                BotAction(message = cho())
            )
        )
    }

    fun install(install: Install) {
        state = state.copy(
            installs = state.installs + install
        )
        saveState()
        startInstall(install)
    }

    fun reinstall(token: String, config: List<BotConfigValue>) {
        state = state.copy(
            installs = state.installs.map {
                if (it.token == token) {
                    it.copy(config = config)
                } else {
                    it
                }
            }
        )
        saveState()

        stopInstall(token)
        state.installs.firstOrNull { it.token == token }?.let {
            startInstall(it)
        }
    }

    fun uninstall(token: String) {
        stopInstall(token)
        state = state.copy(
            installs = state.installs.filter {
                it.token == token
            }
        )
        saveState()
    }

    private suspend fun cho() = dict.dict.entries.random().let { entry ->
        val vn = buildString {
            entry.value.forEach {
                append("${entry.key} (${it.tag})")
                appendLine()
                it.defs.forEach { def ->
                    appendLine()
                    append(def.def)
                    appendLine()
                    def.examples.filter { it.isNotBlank() }.let {
                        if (it.isNotEmpty()) {
                            appendLine()
                            it.forEach {
                                append(it)
                            }
                        }
                    }
                }
            }
        }

        val en = ai.get("""
            Describe this Vietnamese word in English:
            
            $vn
        """.trimIndent())

        buildString {
            append(vn)
            appendLine()
            appendLine()
            en?.let { en ->
                append(en)
                appendLine()
                appendLine()
            }
        }
    }

    private suspend fun runInstall(install: Install) {
        coroutineScope {
            while (true) {
                send(install, cho())
                delay(((install.config.firstOrNull { it.key == "frequency" }?.value?.toFloat() ?: 4f) * 1000 * 60 * 60).toLong())
            }
        }
    }

    private fun CoroutineScope.send(install: Install, message: String) {
        launch {
            println("Sending \"$message\" to ${install.groupId}")
            runCatching {
                http.post(install.webhook) {
                    contentType(ContentType.Application.Json.withCharset(UTF_8))
                    setBody(
                        listOf(
                            BotAction(message = message)
                        )
                    )
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private fun startInstall(install: Install) {
        jobs += install.token to scope.launch {
            runInstall(install)
        }
    }

    private fun stopInstall(token: String) {
        jobs
            .filter { it.first == token }
            .forEach { job ->
                job.second.cancel()
                jobs.remove(job)
            }
    }

    private fun saveState() {
        stateFile.writeText(
            json.encodeToString(state)
        )
    }
}

val app = App()
