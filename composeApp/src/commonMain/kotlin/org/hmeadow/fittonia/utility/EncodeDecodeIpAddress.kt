package org.hmeadow.fittonia.utility

fun encodeIpAddress(ipAddress: String): String {
    try {
        val hexes = ipAddress
            .split('.')
            .also {
                require(it.size == 4)
            }.map {
                it.toInt().also { num ->
                    if (num < 0 || num > 255) throw Exception()
                }.toString(radix = 16).padStart(length = 2, padChar = '0')
            }
        val firstWord = StringBuilder().append(hexes[0][0], hexes[1][0], hexes[2][0]).toString()
        val secondWord = StringBuilder().append(hexes[0][1], hexes[1][1], hexes[2][1]).toString()
        val getWord: (String) -> String = { wordList[it.toInt(radix = 16)] }
        return "${getWord(firstWord)}.${getWord(secondWord)}.${hexes.last().toInt(radix = 16)}"
    } catch (_: Exception) {
        throw IllegalArgumentException(
            "ipAddress \"$ipAddress\" was not in expected format: \"X.X.X.X\" where (0 <= X <= 255)",
        )
    }
}

fun decodeIpAddress(ipAddress: String): String {
    try {
        val tokens = ipAddress.lowercase().split('.').also { require(it.size == 3) }
        val encryptedHex1 = wordList.indexOf(tokens[0]).toString(radix = 16).padStart(length = 3, padChar = '0')
        val encryptedHex2 = wordList.indexOf(tokens[1]).toString(radix = 16).padStart(length = 3, padChar = '0')
        val addressChunk1 = "${encryptedHex1[0]}${encryptedHex2[0]}".toInt(radix = 16)
        val addressChunk2 = "${encryptedHex1[1]}${encryptedHex2[1]}".toInt(radix = 16)
        val addressChunk3 = "${encryptedHex1[2]}${encryptedHex2[2]}".toInt(radix = 16)
        val addressChunk4: Int = tokens[2].toInt().also { require(it in (0..255)) }
        return "$addressChunk1.$addressChunk2.$addressChunk3.$addressChunk4"
    } catch (_: Exception) {
        throw IllegalArgumentException(
            "ipAddress \"$ipAddress\" was not in expected format. (word-word-number)",
        )
    }
}
