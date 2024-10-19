package social.hoc.bot

import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.withCharset
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.text.Charsets.UTF_8

@Serializable
data class OpenAiMessage(
    val role: String = "user",
    val content: String
)

@Serializable
data class OpenAiBody(
    val model: String = "gpt-4o-mini",
    val messages: List<OpenAiMessage>
)

@Serializable
data class ChatCompletion(
    val id: String,
    @SerialName("object") val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage,
    @SerialName("system_fingerprint") val systemFingerprint: String
)

@Serializable
data class Choice(
    val index: Int,
    val message: Message,
    val logprobs: String? = null,
    @SerialName("finish_reason") val finishReason: String
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

class Ai {
    suspend fun get(prompt: String): String? = runCatching {
        http.post("https://api.openai.com/v1/chat/completions") {
            bearerAuth(File("openai").readText().trim().ifBlank { return@runCatching null })
            contentType(ContentType.Application.Json.withCharset(UTF_8))
            setBody(
                OpenAiBody(
                    messages = listOf(
                        OpenAiMessage(
                            role = "system",
                            content = "Do not use markdown or any other formatting in any responses. Respond with plain text only."
                        ),
                        OpenAiMessage(
                            content = prompt
                        )
                    )
                )
            )
        }.body<ChatCompletion>().choices.first().message.content
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()
}

val ai = Ai()
