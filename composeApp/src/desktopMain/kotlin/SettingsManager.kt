import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets

class SettingsManagerDesktop private constructor() : SettingsManager() {

    companion object {
        val settingsManager = SettingsManagerDesktop()
    }

    private val settingsPath =
        "/Users/hunter.wiesman/Desktop/FittoniaTRANSFER/fittoniaSettings|4.xml" // settingsOSSpecificPath // TODO

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

    override fun saveSettings() = synchronized(settings) {
        if (MockConfig.IS_MOCKING) throw IllegalStateException("Attempting to save settings in mock mode.")
        val byteArrayOutputStream = ByteArrayOutputStream()
        jacksonObjectMapper().writeValue(byteArrayOutputStream, settings)
        val encrypted = AESEncyption.encrypt(String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8))
        encrypted.let { File(settingsPath).writeText(encrypted) }
    }
}
