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
    }

    private val settingsPath = "/home/hunterneo/Desktop/TRANSFER/fittoniaSettings.xml"
    val settings = loadSettings()

    private fun loadSettings(): SettingsData {
        val settingsFile = File(settingsPath)
        return if (settingsFile.isFile) {
            val encryptedSettings = settingsFile.readText()
            AESEncyption.decrypt(encryptedSettings)?.let {
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

    class SettingsData(
        val destinations: List<Destination>,
        val dumpPath: String,
    ) {
        constructor() : this(
            destinations = emptyList(),
            dumpPath = "",
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
