package org.hmeadow.fittonia

import SettingsManager
import SettingsManager.Companion.DEFAULT_PORT
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.Serializer
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.hmeadow.fittonia.SettingsDataAndroid.SavableColour
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

val Long.savableColour: SavableColour
    get() = Color(this).serialize
val Color.serialize: SavableColour
    get() = SavableColour(alpha = alpha, red = red, green = green, blue = blue)
val SavableColour.unserialize: Color
    get() = Color(alpha = alpha, red = red, green = green, blue = blue)

@Serializable
data class SettingsDataAndroid(
    val destinations: List<SettingsManager.Destination> = persistentListOf(),
    val dumpPath: DumpPath = DumpPath(),
    val defaultPort: Int = DEFAULT_PORT,
    val temporaryPort: Int? = null,
    val serverAccessCode: String? = null,
    val nextAutoJobName: Long = 0,
    val debugSettings: DebugSettings = DebugSettings(),
) {
    @Serializable
    data class DumpPath(
        val dumpPathForReal: String = "",
        val dumpUriPath: String = "",
        val dumpPathReadable: String = "",
    ) {
        val isSet = dumpUriPath.isNotEmpty() && dumpPathReadable.isNotEmpty()

        constructor(uri: Uri) : this(
            dumpUriPath = uri.path ?: "Error",
            dumpPathReadable = queryName(uri) ?: "Error",
            dumpPathForReal = uri.toString(),
        )
    }

    @Serializable
    data class DebugSettings(
        val defaultSendThrottle: Double = 2.0,
        val defaultNewDestinationName: String = "NewDestination",
        val defaultNewDestinationPort: Int = 12345,
        val defaultNewDestinationAccessCode: String = "accesscode123",
        val defaultNewDestinationIP: String = "0.0.0.0",
        val colourSettings: DebugAppStyleColours = DebugAppStyleColours(),
    )

    @Serializable
    data class DebugAppStyleColours(
        val statusBarColour: SavableColour = 0xFFDDDDDD.savableColour,
        val headerBackgroundColour: SavableColour = 0xFFDDDDDD.savableColour,
        val footerBackgroundColour: SavableColour = 0xFFDDDDDD.savableColour,
        val headerFooterBorderColour: SavableColour = 0xFF9F1555.savableColour,
        val backgroundColour: SavableColour = 0xFFFFFFFF.savableColour,
        val readOnlyBorderColour: SavableColour = 0xFF000000.savableColour,
        val readOnlyBackgroundColour: SavableColour = 0xFFEEEEEE.savableColour,
        val readOnlyClearIconColour: SavableColour = 0xFF000000.savableColour,
        val debugPrimaryButtonColours: DebugPrimaryButtonColours = DebugPrimaryButtonColours(),
        val debugSecondaryButtonColours: DebugSecondaryButtonColours = DebugSecondaryButtonColours(),
        val debugTextInputColours: DebugTextInputColours = DebugTextInputColours(),
        val debugTextColours: DebugTextColours = DebugTextColours(),
    )

    @Serializable
    data class SavableColour(
        val alpha: Float,
        val red: Float,
        val green: Float,
        val blue: Float,
    )

    @Serializable
    data class DebugPrimaryButtonColours(
        val primaryButtonBorderColour: SavableColour = 0xFF000000.savableColour,
        val primaryButtonContentColour: SavableColour = 0xFF000000.savableColour,
        val primaryButtonBackgroundColour: SavableColour = 0xFFFFFFFF.savableColour,
        val primaryButtonDisabledBorderColour: SavableColour = 0xFF000000.savableColour,
        val primaryButtonDisabledContentColour: SavableColour = 0xFF000000.savableColour,
        val primaryButtonDisabledBackgroundColour: SavableColour = 0xFFFFFFFF.savableColour,
    )

    @Serializable
    data class DebugSecondaryButtonColours(
        val secondaryButtonBorderColour: SavableColour = 0xFF000000.savableColour,
        val secondaryButtonContentColour: SavableColour = 0xFF000000.savableColour,
        val secondaryButtonBackgroundColour: SavableColour = 0xFFFFFFFF.savableColour,
        val secondaryButtonDisabledBorderColour: SavableColour = 0xFF000000.savableColour,
        val secondaryButtonDisabledContentColour: SavableColour = 0xFF000000.savableColour,
        val secondaryButtonDisabledBackgroundColour: SavableColour = 0xFFFFFFFF.savableColour,
    )

    @Serializable
    data class DebugTextInputColours(
        val border: SavableColour = 0xFF000000.savableColour,
        val background: SavableColour = 0xFFFFFFFF.savableColour,
        val content: SavableColour = 0xFF000000.savableColour,
        val hint: SavableColour = 0xFF999999.savableColour,
        val label: SavableColour = 0xFF000000.savableColour,
    )

    @Serializable
    data class DebugTextColours(
        val headerTextColour: SavableColour = 0xFF000000.savableColour,
    )
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
