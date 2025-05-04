import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import java.util.Base64
import java.util.LinkedList
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

abstract class SettingsManager {

    companion object {
        const val DEFAULT_PORT = 61113 // Randomly chosen.
    }

    abstract var settings: SettingsData

    abstract fun loadSettings(): SettingsData

    abstract suspend fun saveSettings()

    val settingsMutex = Mutex()

    val defaultPort: Int
        get() = settings.defaultPort

    val previousCmdEntries: LinkedList<String>
        get() = settings.previousCmdEntries

    suspend fun setDumpPath(dumpPath: String) {
        settings = settings.copy(dumpPath = dumpPath)
        saveSettings()
    }

    fun hasDumpPath(): Boolean = settings.dumpPath.isNotEmpty()

    suspend fun addDestination(
        name: String,
        ip: String,
        accessCode: String,
    ) {
        if (settings.destinations.find { it.name == name } != null) {
            throw FittoniaError(FittoniaErrorType.ADD_DESTINATION_ALREADY_EXISTS)
        }
        settings = settings.copy(
            destinations = settings.destinations + listOf(
                Destination(
                    name = name,
                    ip = ip,
                    accessCode = accessCode,
                ),
            ),
        )
        saveSettings()
    }

    suspend fun removeDestination(name: String): Boolean {
        if (settings.destinations.find { it.name == name } == null) {
            return false
        }
        settings = settings.copy(
            destinations = settings.destinations.filterNot { name == it.name },
        )
        saveSettings()
        return true
    }

    suspend fun setDefaultPort(port: Int) {
        settings = settings.copy(defaultPort = port)
        saveSettings()
    }

    suspend fun clearDefaultPort() = setDefaultPort(port = DEFAULT_PORT)

    suspend fun setServerAccessCode(newAccessCode: String) {
        settings = settings.copy(serverAccessCode = newAccessCode)
        saveSettings()
    }

    fun checkAccessCode(accessCode: String): Boolean {
        return settings.serverAccessCode == accessCode
    }

    fun hasServerAccessCode(): Boolean = settings.serverAccessCode != null

    suspend fun getAutoJobName(): String = settingsMutex.withLock {
        settings.nextAutoJobName.let {
            settings = settings.copy(nextAutoJobName = it + 1)
            saveSettings()
            "Job_$it"
        }
    }

    fun findDestination(destinationName: String?): Destination? {
        return destinationName?.let {
            settings.destinations.find {
                it.name == destinationName
            } ?: throw FittoniaError(FittoniaErrorType.DESTINATION_NOT_FOUND, destinationName)
        }
    }

    data class SettingsData(
        val destinations: List<Destination>,
        val dumpPath: String,
        val defaultPort: Int,
        val serverAccessCode: String?,
        val nextAutoJobName: Long,
        val previousCmdEntries: LinkedList<String>,
    ) {
        constructor() : this(
            destinations = emptyList(),
            dumpPath = "",
            defaultPort = DEFAULT_PORT,
            serverAccessCode = null,
            nextAutoJobName = 0,
            previousCmdEntries = LinkedList(),
        )
    }

    @Serializable
    data class Destination(
        val name: String,
        val ip: String,
        val accessCode: String,
    )

    object AESEncyption {
        // Based off of code by Kasım Özdemir.

        // TODO FOR REAL
        private const val secretKey = "U29tZWJvZHkgb25jZSB0b2xkIG1lIHRoZSB3b3JsZCB3YXMgZ29pbmcgdG8gcm9sbCBtZ"
        private const val salt = "QWxsIHRoYXQgZ2xpdHRlcg=="
        private const val iv = "SG9tZSBzd2VldCBwaW5lYQ=="

        fun encrypt(strToEncrypt: String): String {
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
                throw FittoniaError(FittoniaErrorType.ENCRYPTION_ERROR, e.javaClass::class)
            }
        }

        fun decrypt(strToDecrypt: String): String {
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
                throw FittoniaError(FittoniaErrorType.DECRYPTION_ERROR, e.javaClass::class)
            }
        }
    }
}
