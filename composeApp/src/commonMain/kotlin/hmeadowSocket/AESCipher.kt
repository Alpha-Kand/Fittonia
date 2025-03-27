package hmeadowSocket

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESCipher {
    fun generateKey(): ByteArray {
        return KeyGenerator.getInstance(ALGORITHM).run {
            init(SECRET_KEY_SIZE_BITS)
            generateKey()
        }.encoded
    }

    private fun createEncryptCipher(secretKeyBytes: ByteArray): Cipher {
        return createCipher(Cipher.ENCRYPT_MODE, secretKeyBytes)
    }

    private fun createDecryptCipher(secretKeyBytes: ByteArray): Cipher {
        return createCipher(Cipher.DECRYPT_MODE, secretKeyBytes)
    }

    private fun createCipher(mode: Int, secretKeyBytes: ByteArray): Cipher {
        val iv = IvParameterSpec(ByteArray(IV_SIZE_BYTES))
        val cipher = Cipher.getInstance(CIPHER_PARAMS)
        val secretKeySpec = SecretKeySpec(secretKeyBytes, ALGORITHM)
        cipher.init(mode, secretKeySpec, iv)
        return cipher
    }

    fun encryptBytes(bytes: ByteArray, secretKeyBytes: ByteArray): ByteArray {
        return createEncryptCipher(secretKeyBytes = secretKeyBytes).doFinal(bytes)
    }

    fun decryptBytes(bytes: ByteArray, secretKeyBytes: ByteArray): ByteArray {
        return createDecryptCipher(secretKeyBytes = secretKeyBytes).doFinal(bytes)
    }

    private const val ALGORITHM = "AES"
    private const val SECRET_KEY_SIZE_BITS = 256
    private const val CIPHER_PARAMS = "AES/CBC/PKCS5Padding"
    private const val IV_SIZE_BYTES = 16
}
