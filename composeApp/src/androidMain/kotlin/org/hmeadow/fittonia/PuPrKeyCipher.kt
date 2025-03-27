package org.hmeadow.fittonia

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.BLOCK_MODE_ECB
import android.security.keystore.KeyProperties.DIGEST_SHA256
import android.security.keystore.KeyProperties.DIGEST_SHA384
import android.security.keystore.KeyProperties.DIGEST_SHA512
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
import android.security.keystore.KeyProperties.KEY_ALGORITHM_RSA
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.PublicKey
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.RSAKeyGenParameterSpec.F4
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object PuPrKeyCipher {
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEYSTORE_ALIAS = "FittoniaKeysTest5"
    private const val KEY_SIZE = 2048

    // Parameters.
    private const val PADDING = ENCRYPTION_PADDING_RSA_PKCS1
    private const val ALGORITHM = KEY_ALGORITHM_RSA
    private const val BLOCK_MODE = BLOCK_MODE_ECB

    fun encrypt(byteArray: ByteArray, publicKey: ByteArray): ByteArray {
        val mPublicKey = createPublicKeyFromByteArray(publicKey)
        return getCipher().run {
            init(Cipher.ENCRYPT_MODE, mPublicKey)
            doFinal(byteArray)
        }
    }

    fun decrypt(byteArray: ByteArray): ByteArray {
        val privateKey = getPrivateKeyFromKeyStore()?.privateKey ?: throw Exception() // TODO
        return getCipher().run {
            init(Cipher.DECRYPT_MODE, privateKey)
            doFinal(byteArray)
        }
    }

    // TODO - After release -> Super secure mode: Have to enter a shared password not sent over the network.
    // i.e. communicate it through another channel such as physically talking or another messenger.

    fun getPublicKeyFromKeyStore(): PublicKey? {
        return try {
            KeyStore
                .getInstance(ANDROID_KEY_STORE)
                .apply { load(null) }
                .getCertificate(KEYSTORE_ALIAS)
                ?.publicKey
        } catch (e: KeyStoreException) {
            println(e.message)
            null
        }
    }

    fun generateKeyPairs(): KeyPair {
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            /* keystoreAlias = */ KEYSTORE_ALIAS,
            /* purposes = */ PURPOSE_ENCRYPT or PURPOSE_DECRYPT,
        ).run {
            setDigests(DIGEST_SHA256, DIGEST_SHA384, DIGEST_SHA512)
            setAlgorithmParameterSpec(RSAKeyGenParameterSpec(KEY_SIZE, F4))
            setBlockModes(BLOCK_MODE)
            setEncryptionPaddings(PADDING)
            build()
        }
        return KeyPairGenerator.getInstance(
            /* algorithm = */ ALGORITHM,
            /* provider = */ ANDROID_KEY_STORE,
        ).run {
            initialize(parameterSpec)
            generateKeyPair()
        }
    }

    private fun getPrivateKeyFromKeyStore(): KeyStore.PrivateKeyEntry? {
        return try {
            KeyStore
                .getInstance(ANDROID_KEY_STORE)
                .run {
                    load(null)
                    (getEntry(KEYSTORE_ALIAS, null) as KeyStore.PrivateKeyEntry)
                }
        } catch (e: KeyStoreException) {
            println(e.message)
            null
        }
    }

    private fun createPublicKeyFromByteArray(publicKey: ByteArray): PublicKey {
        return KeyFactory.getInstance(ALGORITHM).generatePublic(X509EncodedKeySpec(publicKey))
    }

    private fun getCipher(): Cipher = Cipher.getInstance(
        /* transformation = */ "$ALGORITHM/$BLOCK_MODE/$PADDING",
    )
}
