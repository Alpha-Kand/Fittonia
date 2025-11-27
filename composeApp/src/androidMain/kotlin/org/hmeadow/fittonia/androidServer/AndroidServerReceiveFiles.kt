package org.hmeadow.fittonia.androidServer

import org.hmeadow.fittonia.AppLogs
import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer

internal fun HMeadowSocketServer.serverSharePublicKeys(jobId: Int): PuPrKeyCipher.HMPublicKey {
    val ourPublicKey = PuPrKeyCipher.getPublicKeyFromKeyStore()
        ?: throw IllegalStateException("Server failed getting local public key from keystore.")
    val theirPublicKey = PuPrKeyCipher.HMPublicKey(encoded = receiveByteArray())
    sendByteArray(ourPublicKey.encoded)
    AppLogs.logDebug("Server share public keys success.", jobId)
    return theirPublicKey
}
