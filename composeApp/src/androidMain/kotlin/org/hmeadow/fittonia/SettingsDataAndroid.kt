package org.hmeadow.fittonia

import SettingsManager
import SettingsManager.Companion.DEFAULT_PORT
import androidx.datastore.core.Serializer
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object SettingsDataAndroidSerializer : Serializer<SettingsDataAndroid> {
    override val defaultValue: SettingsDataAndroid
        get() = SettingsDataAndroid()

    override suspend fun readFrom(input: InputStream): SettingsDataAndroid {
        return try {
            Json.decodeFromString(
                deserializer = SettingsDataAndroid.serializer(),
                string = input.readBytes().decodeToString(),
            )
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SettingsDataAndroid, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = SettingsDataAndroid.serializer(),
                value = t,
            ).encodeToByteArray(),
        )
    }
}

@Serializable
data class SettingsDataAndroid(
    val destinations: List<SettingsManager.Destination> = persistentListOf(),
    val dumpPath: String = "",
    val defaultPort: Int = DEFAULT_PORT,
    val serverPassword: String? = null,
    val nextAutoJobName: Long = 0,
)
