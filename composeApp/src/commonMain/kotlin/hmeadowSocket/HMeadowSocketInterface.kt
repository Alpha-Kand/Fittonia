package hmeadowSocket

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.file.Files
import java.time.Instant
import java.util.Arrays
import kotlin.io.path.Path

interface HMeadowSocketInterface {
    var sendBytesPerSecond: Long
    var receiveBytesPerSecond: Long

    fun bindToSocket(block: () -> Socket): Socket
    fun close()

    fun sendInt(message: Int)
    fun receiveInt(): Int

    fun sendLong(message: Long)
    fun receiveLong(): Long

    fun sendBoolean(message: Boolean)
    fun receiveBoolean(): Boolean

    fun sendFile(
        stream: InputStream,
        name: String,
        size: Long,
        rename: String,
        progressPrecision: Double,
        onProgressUpdate: (bytes: Long) -> Unit,
    )

    fun sendFile(
        filePath: String,
        rename: String = "",
        progressPrecision: Double,
        onProgressUpdate: (bytes: Long) -> Unit,
    )

    fun receiveFile(
        destination: String,
        prefix: String,
        suffix: String,
    ): Pair<String, String>

    fun receiveFile(
        onOutputStream: (fileName: String) -> OutputStream?,
        progressPrecision: Double,
        beforeDownload: (totalBytes: Long, name: String) -> Unit,
        onProgressUpdate: (progress: Long) -> Unit,
    )

    fun sendString(message: String)
    fun receiveString(): String

    fun sendContinue()
    fun receiveContinue()
}

open class HMeadowSocketInterfaceReal : HMeadowSocketInterface {
    override var sendBytesPerSecond: Long = Long.MAX_VALUE
    override var receiveBytesPerSecond: Long = Long.MAX_VALUE

    companion object {
        private const val BUFFER_SIZE_LONG: Long = 8192
        const val BUFFER_SIZE_INT: Int = 8192
    }

    private lateinit var mDataInput: DataInputStream
    private lateinit var mDataOutput: DataOutputStream

    override fun bindToSocket(block: () -> Socket): Socket {
        val socket = block()
        mDataInput = DataInputStream(socket.getInputStream())
        mDataOutput = DataOutputStream(socket.getOutputStream())
        return socket
    }

    override fun close() {
        mDataInput.close()
        mDataOutput.close()
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

    override fun sendFile(
        stream: InputStream,
        name: String,
        size: Long,
        rename: String,
        progressPrecision: Double,
        onProgressUpdate: (bytes: Long) -> Unit,
    ) {
        val bufferedReadFile = BufferedInputStream(stream)

        // 1. Send file size in bytes.
        sendLong(size)
        val step = (size * progressPrecision).toLong()
        var currentStep = step

        // 2. Send file name plus trailing whitespace.
        sendString(rename.takeIf { it.isNotEmpty() } ?: name)

        // 3. Send the file.
        var remainingBytes = size

        var throttle = sendBytesPerSecond
        var now = Instant.now().toEpochMilli()
        while (remainingBytes > 0) {
            val nextBytes = minOf(remainingBytes, BUFFER_SIZE_LONG, throttle)
            throttle -= nextBytes
            mDataOutput.write(bufferedReadFile.readNBytes(nextBytes.toInt()))
            remainingBytes -= nextBytes
            if ((size - remainingBytes) > currentStep) {
                currentStep += step
                onProgressUpdate(size - remainingBytes)
            }
            if (throttle == 0L) {
                onProgressUpdate(size - remainingBytes)
                throttle = sendBytesPerSecond
                Thread.sleep((1000L - ((Instant.now().toEpochMilli() - now).coerceAtLeast(minimumValue = 0))))
                now = Instant.now().toEpochMilli()
            }
        }

        bufferedReadFile.close()
    }

    override fun sendFile(
        filePath: String,
        rename: String,
        progressPrecision: Double,
        onProgressUpdate: (bytes: Long) -> Unit,
    ) {
        val path = Path(filePath)
        val size = Files.size(path)
        sendFile(
            stream = File(filePath).inputStream(),
            name = path.fileName.toString(),
            size = size,
            rename = rename,
            progressPrecision = progressPrecision,
            onProgressUpdate = onProgressUpdate,
        )
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

    override fun receiveFile(
        onOutputStream: (fileName: String) -> OutputStream?,
        progressPrecision: Double,
        beforeDownload: (totalBytes: Long, fileName: String) -> Unit,
        onProgressUpdate: (progress: Long) -> Unit,
    ) {
        // 1. Get total file size in bytes.
        val size = receiveLong()
        var transferByteCount = size
        // 2. Get file name.
        val fileName = receiveString()
        beforeDownload(transferByteCount, fileName)
        // 3. Receive and write file data.
        val step = (transferByteCount * progressPrecision).toLong()
        var currentStep = step
        var remainingBytes = transferByteCount
        onOutputStream(fileName)?.use { stream ->
            if (transferByteCount == 0L) {
                // File to transfer is empty, just create a new empty file.
            } else {
                while (transferByteCount > 0) {
                    // Read next amount of data from socket.
                    val readByteArray = readNBytes(length = transferByteCount.coerceAtMost(BUFFER_SIZE_LONG).toInt())
                    if (readByteArray.isNotEmpty()) {
                        // Write data to file.
                        transferByteCount -= BUFFER_SIZE_LONG
                        stream.write(readByteArray)
                        remainingBytes -= readByteArray.size
                        if ((size - remainingBytes) > currentStep) {
                            currentStep += step
                            onProgressUpdate(size - remainingBytes)
                        }
                    }
                }
            }
        }
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
    fun readNBytes(length: Int): ByteArray {
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
