package org.hmeadow.fittonia

import SettingsManager
import SettingsManager.Companion.DEFAULT_PORT
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
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
    val dumpPath: DumpPath = DumpPath(),
    val defaultPort: Int = DEFAULT_PORT,
    val serverPassword: String? = null,
    val nextAutoJobName: Long = 0,
) {
    @Serializable
    data class DumpPath(
        val dumpPathUri: String = "",
        val dumpPathReadable: String = "",
    ) {
        val isSet = dumpPathUri.isNotEmpty() && dumpPathReadable.isNotEmpty()

        constructor(uri: Uri) : this(
            dumpPathUri = uri.path ?: "Error",
            dumpPathReadable = queryName(uri) ?: "Error",
        )
    }
}

private fun queryName(uri: Uri): String? {
    return DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))?.let {
        val returnCursor = MainActivity
            .mainActivity
            .contentResolver
            .query(it, null, null, null, null) ?: return ""
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        name
    }
}
