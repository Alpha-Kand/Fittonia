package hmeadowSocket

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.nio.file.Files
import java.util.Arrays
import kotlin.io.path.Path

interface HMeadowSocketInterface {
    fun bindToSocket(block: () -> Socket): Socket
    fun close()

    fun sendInt(message: Int)
    fun receiveInt(): Int

    fun sendLong(message: Long)
    fun receiveLong(): Long

    fun sendBoolean(message: Boolean)
    fun receiveBoolean(): Boolean

    fun sendFile(filePath: String, rename: String = "")
    fun receiveFile(
        destination: String,
        prefix: String,
        suffix: String,
    ): Pair<String, String>

    fun sendString(message: String)
    fun receiveString(): String

    fun sendContinue()
    fun receiveContinue()
}

open class HMeadowSocketInterfaceReal : HMeadowSocketInterface {

    companion object {
        private const val BUFFER_SIZE_LONG: Long = 8192
        private const val BUFFER_SIZE_INT: Int = 8192
    }

    private lateinit var mDataInput: DataInputStream
    private lateinit var mDataOutput: DataOutputStream
    private lateinit var mInputStreamReader: BufferedReader

    override fun bindToSocket(block: () -> Socket): Socket {
        val socket = block()
        mDataInput = DataInputStream(socket.getInputStream())
        mDataOutput = DataOutputStream(socket.getOutputStream())
        mInputStreamReader = BufferedReader(InputStreamReader(socket.getInputStream()))
        return socket
    }

    override fun close() {
        mDataInput.close()
        mDataOutput.close()
        mInputStreamReader.close()
    }

    override fun sendInt(message: Int) = mDataOutput.writeInt(message)
    override fun receiveInt() = mDataInput.readInt()

    override fun sendLong(message: Long) = mDataOutput.writeLong(message)
    override fun receiveLong() = mDataInput.readLong()

    override fun sendBoolean(message: Boolean) = mDataOutput.writeBoolean(message)
    override fun receiveBoolean() = mDataInput.readBoolean()

    override fun sendString(message: String) {
        val byteArray = message.encodeToByteArray()
        val totalBytes = byteArray.size
        sendInt(message = totalBytes)
        mDataOutput.write(byteArray, 0, totalBytes)
    }

    override fun receiveString(): String {
        val messageLength = receiveInt()
        return String(readNBytes(messageLength), Charsets.UTF_8)
    }

    override fun sendFile(filePath: String, rename: String) {
        val bufferedReadFile = BufferedInputStream(File(filePath).inputStream())
        val path = Path(filePath)
        val size = Files.size(path)

        // 1. Send file size in bytes.
        sendLong(size)

        // 2. Send file name plus trailing whitespace.
        sendString(rename.takeIf { it.isNotEmpty() } ?: path.fileName.toString())

        // 3. Send the file.
        var remainingBytes = size
        while (remainingBytes > 0) {
            val nextBytes = remainingBytes.coerceAtLeast(BUFFER_SIZE_LONG)
            mDataOutput.write(bufferedReadFile.readNBytes(nextBytes.toInt()))
            remainingBytes -= nextBytes
        }

        bufferedReadFile.close()
    }

    override fun receiveFile(
        destination: String,
        prefix: String,
        suffix: String,
    ): Pair<String, String> {
        /* Must receive the following information in order.
           1. (8 byte int) File size in bytes.
           2. (127 char bytes) File name. Character bytes at beginning of buffer, then blank spaces
              up to max file name size.
           3. (File data bytes) Should consist of full blocks of buffer size plus one partially
              filled buffer to finish the file transfer.
         */

        // 1. Get total file size in bytes.
        var transferByteCount = receiveLong()
        // 2. Get file name.
        val fileName = receiveString()
        // 3. Receive and write file data.
        val file = if (destination.isEmpty()) {
            File.createTempFile(prefix, suffix)
        } else {
            File.createTempFile(prefix, suffix, File(Path("$destination/").toString()))
        }
        if (transferByteCount == 0L) {
            // File to transfer is empty, just create a new empty file.
        } else {
            while (transferByteCount > 0) {
                // Read next amount of data from socket.
                val readByteArray = readNBytes(transferByteCount.coerceAtMost(BUFFER_SIZE_LONG).toInt())
                if (readByteArray.isNotEmpty()) {
                    // Write data to file.
                    transferByteCount -= BUFFER_SIZE_LONG
                    file.appendBytes(readByteArray)
                }
            }
        }

        return file.absolutePath to fileName
    }

    override fun sendContinue() = sendBoolean(message = true)
    override fun receiveContinue() {
        receiveBoolean()
    }

    /**
     * VIRTUAL COPY AND PASTE OF OFFICIAL 'readNBytes' METHOD FOR ANDROID JAVA 8 COMPATIBILITY.
     *
     * Reads up to a specified number of bytes from the input stream. This
     * method blocks until the requested number of bytes have been read, end
     * of stream is detected, or an exception is thrown. This method does not
     * close the input stream.
     *
     * The length of the returned array equals the number of bytes read
     * from the stream. If {@code len} is zero, then no bytes are read and
     * an empty byte array is returned. Otherwise, up to {@code len} bytes
     * are read from the stream. Fewer than {@code len} bytes may be read if
     * end of stream is encountered.
     *
     * When this stream reaches end of stream, further invocations of this
     * method will return an empty byte array.
     *
     * Note that this method is intended for simple cases where it is
     * convenient to read the specified number of bytes into a byte array. The
     * total amount of memory allocated by this method is proportional to the
     * number of bytes read from the stream which is bounded by {@code len}.
     * Therefore, the method may be safely called with very large values of
     * {@code len} provided sufficient memory is available.
     *
     * The behavior for the case where the input stream is <i>asynchronously
     * closed</i>, or the thread interrupted during the read, is highly input
     * stream specific, and therefore not specified.
     *
     * If an I/O error occurs reading from the input stream, then it may do
     * so after some, but not all, bytes have been read. Consequently, the input
     * stream may not be at end of stream and may be in an inconsistent state.
     * It is strongly recommended that the stream be promptly closed if an I/O
     * error occurs.
     *
     * @implNote
     * The number of bytes allocated to read data from this stream and return
     * the result is bounded by {@code 2*(long)len}, inclusive.
     *
     * @param length the maximum number of bytes to read
     * @return a byte array containing the bytes read from this input stream
     * @throws IllegalArgumentException if {@code length} is negative
     * @throws IOException if an I/O error occurs
     * @throws OutOfMemoryError if an array of the required size cannot be
     *         allocated.
     */
    @Throws(IOException::class)
    private fun readNBytes(length: Int): ByteArray {
        require(length >= 0) { "length <= 0" }
        // List of buffers which may or may not be full (Max BUFFER_SIZE).
        var bufferList: MutableList<ByteArray>? = null
        // The end buffer we will return with the read-in bytes.
        var resultBuffer: ByteArray? = null
        // Keeps track of how many bytes have been read in total.
        var totalReadBytes = 0
        // How many bytes we still need to read.
        var remainingBytes = length
        var n: Int
        do {
            // Create a buffer for this iteration of reading.
            val buffer = ByteArray(remainingBytes.coerceAtMost(BUFFER_SIZE_INT))
            // Bytes read this iteration.
            var readBytes = 0
            // Read until EOF, which may be more or less than buffer size.
            while (mDataInput.read(
                    buffer,
                    readBytes,
                    (buffer.size - readBytes).coerceAtMost(remainingBytes),
                ).also { n = it } > 0
            ) {
                readBytes += n
                remainingBytes -= n
            }
            // If some bytes were read...
            if (readBytes > 0) {
                // Read too many bytes, throw error.
                if (2147483639 - totalReadBytes < readBytes) {
                    throw OutOfMemoryError("Required array size too large")
                }
                totalReadBytes += readBytes
                if (resultBuffer == null) {
                    // The first buffer is set to 'resultBuffer'.
                    resultBuffer = buffer
                } else {
                    // If the amount of bytes exceeds BUFFER_SIZE or there are at least two read iterations, add the
                    // working buffers to the list.
                    if (bufferList == null) {
                        bufferList = ArrayList()
                        bufferList.add(resultBuffer)
                    }
                    bufferList.add(buffer)
                }
            }
            // If the last call to read returned -1 or the number of bytes requested have been read, then break.
        } while (n >= 0 && remainingBytes > 0)

        // If there was only one iteration of reading...
        if (bufferList == null) {
            if (resultBuffer == null) {
                // No bytes could be read, return an empty buffer.
                return ByteArray(0)
            }
            // Return the entire result buffer if it holds the perfect amount, otherwise only return the relevant sub
            // array of bytes.
            return if (resultBuffer.size == totalReadBytes) {
                resultBuffer
            } else {
                Arrays.copyOf(resultBuffer, totalReadBytes)
            }
        }
        // Reset the resultBuffer.
        resultBuffer = ByteArray(totalReadBytes)
        var offset = 0
        remainingBytes = totalReadBytes
        // For all the bytes in the bufferList, copy their bytes into one buffer.
        for (b in bufferList) {
            val count = b.size.coerceAtMost(remainingBytes)
            System.arraycopy(b, 0, resultBuffer, offset, count)
            offset += count
            remainingBytes -= count
        }
        return resultBuffer
    }
}

class HMeadowSocketInterfaceRealDebug(
    private val tag: String,
    private val print: (String) -> Unit = ::println,
) : HMeadowSocketInterfaceReal() {
    private var shush = false
    private var count = 1
    private fun printStatus(message: String) = print("$count tag:$tag $message").also { count++ }

    override fun bindToSocket(block: () -> Socket): Socket {
        printStatus("bindToSocket")
        return super.bindToSocket(block)
    }

    override fun close() {
        printStatus("close")
        super.close()
    }

    override fun sendInt(message: Int) {
        if (!shush) {
            printStatus("sendInt = $message")
        }
        super.sendInt(message)
    }

    override fun receiveInt(): Int {
        val value = super.receiveInt()
        if (!shush) {
            printStatus("receiveInt = $value")
        }
        return value
    }

    override fun sendLong(message: Long) {
        printStatus("sendLong = $message")
        super.sendLong(message)
    }

    override fun receiveLong(): Long {
        val value = super.receiveLong()
        printStatus("receiveLong = $value")
        return value
    }

    override fun sendBoolean(message: Boolean) {
        printStatus("sendBoolean = $message")
        super.sendBoolean(message)
    }

    override fun receiveBoolean(): Boolean {
        val value = super.receiveBoolean()
        printStatus("receiveBoolean = $value")
        return value
    }

    override fun sendFile(filePath: String, rename: String) {
        printStatus("sendFile = (filePath: $filePath ) (rename: $rename )")
        super.sendFile(filePath, rename)
    }

    override fun receiveFile(
        destination: String,
        prefix: String,
        suffix: String,
    ): Pair<String, String> {
        val value = super.receiveFile(destination, prefix, suffix)
        printStatus("receiveFile = (first: ${value.first} ) (second: ${value.second} )")
        return value
    }

    override fun sendString(message: String) {
        shush = true
        super.sendString(message)
        printStatus("sendString = $message")
        shush = false
    }

    override fun receiveString(): String {
        shush = true
        val value = super.receiveString()
        shush = false
        printStatus("receiveString = $value")
        return value
    }

    override fun sendContinue() {
        printStatus("sendContinue")
        super.sendContinue()
    }

    override fun receiveContinue() {
        printStatus("receiveContinue")
        super.receiveContinue()
    }
}
