package social.hoc.bot

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Definition(
    val tag: String,
    val defs: List<Def>
)

@Serializable
data class Def(
    val def: String,
    val examples: List<String>
)

class Dict {

    val dict: Map<String, List<Definition>>

    init {
        File("underthesea_dictionary.yaml").inputStream().let {
            println("Loading dictionary")

            dict = Yaml(configuration = YamlConfiguration(codePointLimit = Int.MAX_VALUE)).decodeFromStream(it)

            println("${dict.size} entries")
        }
    }
}

val dict = Dict()
