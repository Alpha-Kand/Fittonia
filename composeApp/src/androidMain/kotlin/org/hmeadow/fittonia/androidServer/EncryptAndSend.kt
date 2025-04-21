package org.hmeadow.fittonia.androidServer

import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.PuPrKeyCipher.ENCRYPT_MAX_BYTES_ALLOWED
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocket
import org.hmeadow.fittonia.utility.byteArray
import org.hmeadow.fittonia.utility.toJSONByteArray

fun <T> HMeadowSocket.encryptAndSend(data: T, theirPublicKey: PuPrKeyCipher.HMPublicKey) {
    // Prepare.
    val dataJsonBytes = data.toJSONByteArray
    var encryptedData = ByteArray(0)
    val intermediateEncryptedDataChunkCount = dataJsonBytes.size / ENCRYPT_MAX_BYTES_ALLOWED

    // Encrypt data in chunks.
    repeat(intermediateEncryptedDataChunkCount) { chunkIndex ->
        encryptedData += PuPrKeyCipher.encrypt(
            byteArray = dataJsonBytes.copyOfRange(
                chunkIndex * ENCRYPT_MAX_BYTES_ALLOWED,
                (chunkIndex + 1) * ENCRYPT_MAX_BYTES_ALLOWED,
            ),
            publicKey = theirPublicKey,
        )
    }
    encryptedData += PuPrKeyCipher.encrypt(
        byteArray = dataJsonBytes.copyOfRange(
            intermediateEncryptedDataChunkCount * ENCRYPT_MAX_BYTES_ALLOWED,
            dataJsonBytes.size,
        ),
        publicKey = theirPublicKey,
    )

    // Send payload.
    val encryptedDataSize = PuPrKeyCipher.encrypt(byteArray = encryptedData.size.byteArray, publicKey = theirPublicKey)
    sendByteArrayRaw(encryptedDataSize)
    sendByteArrayRaw(encryptedData)
}
