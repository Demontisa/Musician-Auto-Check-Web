package it.xiyan.automusician

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mu.KotlinLogging
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {  }

object Constants {
    val FILE_SERVER_PROPERTIES = File("./config.json")
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    val serverProp = loadServerProperties()
}

private val adapter = Constants.moshi.adapter(ServerProperties::class.javaObjectType)
    .serializeNulls()

fun loadServerProperties(): ServerProperties {
    return if (!Constants.FILE_SERVER_PROPERTIES.exists()) {
        logger.warn { "The configuration file does not exist. Run the server with the default configuration." }
        ServerProperties.DEFAULT
    } else {
        try {
            val properties = (adapter.fromJson(Constants.FILE_SERVER_PROPERTIES.readText(StandardCharsets.UTF_8)) ?: ServerProperties.DEFAULT)
            if (properties == ServerProperties.DEFAULT) {
                logger.warn { "The configuration file is empty. Use the default configuration file to run the server." }
            } else {
                logger.info { "Successfully loaded configuration file. " +
                        "(Path: ${Constants.FILE_SERVER_PROPERTIES.canonicalPath})" }
            }
            properties
        } catch (e: IOException) {
            logger.error(e) { "An error occurred while loading the configuration file." }
            ServerProperties.DEFAULT
        }
    }
}

fun initialConfigFile(configFile: File = Constants.FILE_SERVER_PROPERTIES) {
    configFile.writeText(adapter.toJson(ServerProperties.DEFAULT), StandardCharsets.UTF_8)
}

data class ServerProperties(
    val apiServer: String = "https://netease-cloud-music-api-binaryify.vercel.app/",
    val bindAddress: String = "0.0.0.0",
    val httpPort: Int = 8080,
    val database: DatabaseConnectConfig = DatabaseConnectConfig(),
    val httpProxy: HttpProxy = HttpProxy()
) {
    companion object {
        val DEFAULT = ServerProperties()
    }
}

data class DatabaseConnectConfig(
    val address: String = "localhost",
    val databaseName: String = "auto_musician",
    val user: String? = "root",
    val password: String? = ""
)

data class HttpProxy(val enable: Boolean = false, val host: String = "127.0.0.1", val port: Int = 1080)
