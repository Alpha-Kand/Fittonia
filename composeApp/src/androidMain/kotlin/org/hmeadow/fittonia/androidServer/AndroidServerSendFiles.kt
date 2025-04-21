package org.hmeadow.fittonia.androidServer

import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient

fun clientSharePublicKeys(client: HMeadowSocketClient): PuPrKeyCipher.HMPublicKey {
    // Share public keys.
    val ourPublicKey = PuPrKeyCipher.getPublicKeyFromKeyStore() ?: throw Exception() // TODO - after releaswe
    client.sendByteArray(ourPublicKey.encoded)
    return PuPrKeyCipher.HMPublicKey(encoded = client.receiveByteArray())
}
