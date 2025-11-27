package org.hmeadow.fittonia

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class TestSocket(
    private val inputStream: InputStream = TestInputStream(),
    private val outputStream: OutputStream = TestOutputStream(),
) : Socket() {
    override fun getInputStream(): InputStream {
        return inputStream
    }

    override fun getOutputStream(): OutputStream {
        return outputStream
    }
}
