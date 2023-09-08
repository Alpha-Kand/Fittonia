package settingsManager

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class SettingsManager private constructor() {

    companion object {
        val settingsManager = SettingsManager()
        private const val DEFAULT_PORT = 61113 // Randomly chosen.
    }

    private val settingsPath = "/home/hunterneo/Desktop/TRANSFER/fittoniaSettings|3.xml" // TODO
    var settings = loadSettings()
        private set

    var defaultPort = settings.defaultPort
        private set

    private fun loadSettings(): SettingsData {
        val settingsFile = File(settingsPath)
        return if (settingsFile.isFile) {
            val decryptedText = AESEncyption.decrypt(settingsFile.readText())
            decryptedText?.let {
                jacksonObjectMapper().readValue<SettingsData>(it)
            } ?: throw IllegalStateException("")
        } else {
            SettingsData()
        }
    }

    fun saveSettings() {
        val byteArrayOutputStream = ByteArrayOutputStream()
        jacksonObjectMapper().writeValue(byteArrayOutputStream, settings)

        val encrypted = AESEncyption.encrypt(String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8))
        encrypted?.let { File(settingsPath).writeText(encrypted) }
    }

    fun setDumpPath(dumpPath: String) {
        settings = settings.copy(dumpPath = dumpPath)
        saveSettings()
    }

    fun addDestination(
        name: String,
        ip: String,
        password: String,
    ) {
        if (settings.destinations.find { it.name == name } != null) {
            throw IllegalArgumentException(
                "A destination with that name is already registered. Delete the old one and try again.",
            )
        }
        settings = settings.copy(
            destinations = settings.destinations + listOf(
                SettingsData.Destination(
                    name = name,
                    ip = ip,
                    password = password,
                ),
            ),
        )
        saveSettings()
    }

    fun removeDestination(name: String) {
        if (settings.destinations.find { it.name == name } == null) {
            throw IllegalArgumentException("Destination with that name not found.")
        }
        settings = settings.copy(
            destinations = settings.destinations.filterNot { name == it.name },
        )
        saveSettings()
    }

    fun setDefaultPort(port: Int) {
        settings = settings.copy(defaultPort = port)
        saveSettings()
    }

    fun clearDefaultPort() = setDefaultPort(port = DEFAULT_PORT)

    fun setServerPassword(newPassword: String) {
        settings = settings.copy(serverPassword = newPassword)
        saveSettings()
    }

    fun checkPassword(password: String): Boolean {
        return settings.serverPassword == password
    }

    data class SettingsData(
        val destinations: List<Destination>,
        val dumpPath: String,
        val defaultPort: Int,
        val serverPassword: String,
    ) {
        constructor() : this(
            destinations = emptyList(),
            dumpPath = "",
            defaultPort = DEFAULT_PORT,
            serverPassword = "",
        )

        data class Destination(
            val name: String,
            val ip: String,
            val password: String,
        )
    }
}

private object AESEncyption {
    // Based off of code by Kasım Özdemir.

    private const val secretKey = "U29tZWJvZHkgb25jZSB0b2xkIG1lIHRoZSB3b3JsZCB3YXMgZ29pbmcgdG8gcm9sbCBtZ"
    private const val salt = "QWxsIHRoYXQgZ2xpdHRlcg=="
    private const val iv = "SG9tZSBzd2VldCBwaW5lYQ=="

    fun encrypt(strToEncrypt: String): String? {
        try {
            val decoder = Base64.getDecoder()
            val ivParameterSpec = IvParameterSpec(decoder.decode(iv))
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = PBEKeySpec(secretKey.toCharArray(), decoder.decode(salt), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)))
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return null
    }

    fun decrypt(strToDecrypt: String): String? {
        try {
            val decoder = Base64.getDecoder()
            val ivParameterSpec = IvParameterSpec(decoder.decode(iv))
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = PBEKeySpec(secretKey.toCharArray(), decoder.decode(salt), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            return String(cipher.doFinal(decoder.decode(strToDecrypt)))
        } catch (e: Exception) {
            println("Error while decrypting: $e")
        }
        return null
    }
}
