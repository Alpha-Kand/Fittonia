package org.hmeadow.fittonia.androidServer

import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.PuPrKeyCipher.ENCRYPT_OUT_BYTES
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocket
import org.hmeadow.fittonia.utility.convertToObject
import java.nio.ByteBuffer
import kotlin.math.min

inline fun <reified T> HMeadowSocket.receiveAndDecrypt(): T {
    // Get incoming.
    val encryptedDataSize = ByteBuffer.wrap(PuPrKeyCipher.decrypt(receiveByteArrayRaw(ENCRYPT_OUT_BYTES))).getInt()
    val data = receiveByteArrayRaw(encryptedDataSize)
    var bytes = ByteArray(0)
    repeat(encryptedDataSize / ENCRYPT_OUT_BYTES) {
        bytes += PuPrKeyCipher.decrypt(
            byteArray = data.copyOfRange(
                fromIndex = it * ENCRYPT_OUT_BYTES,
                toIndex = min((it + 1) * ENCRYPT_OUT_BYTES, data.size),
            ),
        )
    }
    return convertToObject(data = bytes)
}
