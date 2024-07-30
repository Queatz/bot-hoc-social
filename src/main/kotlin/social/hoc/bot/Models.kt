package social.hoc.bot

import kotlinx.serialization.Serializable

@Serializable
data class BotConfigValue(
    var key: String? = null,
    var value: String? = null
)

@Serializable
data class BotConfigField(
    var key: String? = null,
    var label: String? = null,
    var placeholder: String? = null,
    var type: String? = null,
    var required: Boolean? = null
)

@Serializable
data class BotDetails(
    val name: String? = null,
    val description: String? = null,
    val keywords: List<String>? = null,
    val config: List<BotConfigField>? = null,
)

@Serializable
data class InstallBotResponse(
    val token: String
)

@Serializable
data class InstallBotBody(
    val groupId: String,
    val groupName: String,
    val webhook: String,
    val config: List<BotConfigValue>? = null,
    val secret: String? = null
)

@Serializable
data class ReinstallBotBody(
    val config: List<BotConfigValue>? = null,
)

@Serializable
data class MessageBotResponse(
    val success: Boolean? = null,
    val note: String? = null,
    val actions: List<BotAction>? = null
)

@Serializable
data class BotAction(
    val message: String? = null
)

@Serializable
data class MessageBotBody(
    val message: String? = null,
    val person: Person? = null
)


@Serializable
class Person(
    var id: String? = null,
    var name: String? = null
)
