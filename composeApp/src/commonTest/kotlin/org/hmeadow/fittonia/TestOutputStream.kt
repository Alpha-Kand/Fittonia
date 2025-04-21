package org.hmeadow.fittonia

import java.io.OutputStream

class TestOutputStream : OutputStream() {
    private val buffer = mutableListOf<Int>()

    fun getByteArray(): ByteArray {
        val byteArray = ByteArray(buffer.size)
        buffer.forEachIndexed { index, byte ->
            byteArray[index] = byte.toByte()
        }
        return byteArray
    }

    override fun write(b: Int) {
        buffer.add(b)
    }
}
