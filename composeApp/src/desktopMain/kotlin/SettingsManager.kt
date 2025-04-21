import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.sync.withLock
import org.hmeadow.fittonia.utility.toJSONByteArray
import org.hmeadow.fittonia.utility.toString
import java.io.File

class SettingsManagerDesktop private constructor() : SettingsManager() {

    companion object {
        val settingsManager = SettingsManagerDesktop()
    }

    private val settingsPath = // TODO - After release
        "/Users/hunter.wiesman/Desktop/FittoniaTRANSFER/fittoniaSettings|4.xml" // settingsOSSpecificPath

    override var settings = loadSettings()

    override fun loadSettings(): SettingsData {
        return if (!MockConfig.IS_MOCKING) {
            val settingsFile = File(settingsPath)
            if (settingsFile.isFile) {
                val decryptedText = AESEncyption.decrypt(settingsFile.readText())
                decryptedText.let { jacksonObjectMapper().readValue<SettingsData>(it) }
            } else {
                SettingsData()
            }
        } else {
            SettingsData()
        }
    }

    override suspend fun saveSettings() = settingsMutex.withLock {
        if (MockConfig.IS_MOCKING) throw IllegalStateException("Attempting to save settings in mock mode.")
        // TODO after release - Why convert to bytearray and then back to string.
        val encrypted = AESEncyption.encrypt(settings.toJSONByteArray.toString)
        encrypted.let { File(settingsPath).writeText(encrypted) }
    }
}
