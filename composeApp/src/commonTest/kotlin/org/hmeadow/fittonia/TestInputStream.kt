package org.hmeadow.fittonia

import java.io.InputStream

class TestInputStream : InputStream() {
    private var input = ByteArray(1)
    private var index = -1

    fun setBuffer(buffer: ByteArray) {
        input = buffer
        index = 0
    }

    fun setFileBytes(buffer: ByteArray) {
        input = buffer + ByteArray(size = 1).apply { this[0] = -1 }
        index = 0
    }

    override fun read(): Int {
        return input[index++].toInt()
    }
}
