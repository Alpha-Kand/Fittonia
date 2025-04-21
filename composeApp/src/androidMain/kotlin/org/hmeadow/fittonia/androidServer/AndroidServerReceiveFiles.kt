package org.hmeadow.fittonia.androidServer

import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer

fun AndroidServer.serverSharePublicKeys(server: HMeadowSocketServer, jobId: Int): PuPrKeyCipher.HMPublicKey {
    // Share public keys.
    val ourPublicKey = PuPrKeyCipher.getPublicKeyFromKeyStore()
    if (ourPublicKey == null) {
        log("Server failed getting local public key from keystore.", jobId)
        throw Exception() // TODO - after release
    }
    val theirPublicKey = PuPrKeyCipher.HMPublicKey(encoded = server.receiveByteArray())
    server.sendByteArray(ourPublicKey.encoded)
    log("Server share public keys success.", jobId)
    return theirPublicKey
}
