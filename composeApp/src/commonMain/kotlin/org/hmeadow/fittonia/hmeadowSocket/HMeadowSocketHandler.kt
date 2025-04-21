package org.hmeadow.fittonia.hmeadowSocket

import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketHandler.Now.now
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketHandler.Sleeper.sleep
import org.hmeadow.fittonia.utility.readNBytesHM
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.Path

open class HMeadowSocketHandler {

    data object Sleeper {
        fun sleep(millis: Long) {
            Thread.sleep(millis)
        }
    }

    data object Now {
        fun now(): Long {
            return Instant.now().toEpochMilli()
        }
    }

    data object FilesObject {
        fun size(path: Path): Long {
            return Files.size(path)
        }

        fun inputStream(filePath: String): InputStream {
            return File(filePath).inputStream()
        }
    }

    open var sendBytesPerSecond: Long = Long.MAX_VALUE
    open var receiveBytesPerSecond: Long = Long.MAX_VALUE

    companion object {
        private const val BUFFER_SIZE_LONG: Long = 8192
        private const val BUFFER_SIZE_INT: Int = 8192

        private const val CIPHER_BLOCK_SIZE_INT: Int = 128
        private const val CIPHER_BLOCK_SIZE_LONG: Long = 128
    }

    open lateinit var mDataInput: DataInputStream
    open lateinit var mDataOutput: DataOutputStream

    open fun bindToSocket(block: () -> Socket): Socket {
        val socket = block()
        mDataInput = DataInputStream(socket.getInputStream())
        mDataOutput = DataOutputStream(socket.getOutputStream())
        return socket
    }

    open fun close() {
        mDataInput.close()
        mDataOutput.close()
    }

    open fun sendInt(message: Int) = mDataOutput.writeInt(message)
    open fun receiveInt() = mDataInput.readInt()

    open fun sendLong(message: Long) = mDataOutput.writeLong(message)
    open fun receiveLong() = mDataInput.readLong()

    open fun sendBoolean(message: Boolean) = mDataOutput.writeBoolean(message)
    open fun receiveBoolean() = mDataInput.readBoolean()

    open fun sendString(message: String) {
        val byteArray = message.encodeToByteArray()
        val totalBytes = byteArray.size
        sendInt(message = totalBytes)
        mDataOutput.write(byteArray, 0, totalBytes)
    }

    open fun receiveString(): String {
        val messageLength = receiveInt()
        return String(mDataInput.readNBytesHM(messageLength), Charsets.UTF_8)
    }

    open fun sendFile(
        stream: InputStream,
        name: String,
        size: Long,
        encryptBlock: (ByteArray) -> ByteArray,
        progressPrecision: Double,
        onProgressUpdate: (bytes: Long) -> Unit,
    ) {
        val bufferedReadFile = BufferedInputStream(stream)

        // 1. Send file size in bytes.
        sendLong(size)
        val step = (size * progressPrecision).toLong()
        var currentStep = step

        // 2. Send file name.
        sendString(name.takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException("Name should not be empty."))

        // 3. Send the file.
        var remainingBytes = size

        var throttle = sendBytesPerSecond
        var now = now()
        while (remainingBytes > 0) {
            val buffer = ByteArray(BUFFER_SIZE_INT)
            var offset = 0
            var bufferFilled = 0

            while (offset < BUFFER_SIZE_INT / CIPHER_BLOCK_SIZE_INT) {
                if (remainingBytes == 0L) {
                    break
                }
                val nextBytes = minOf(remainingBytes, CIPHER_BLOCK_SIZE_LONG)
                val readBytes = encryptBlock(bufferedReadFile.readNBytes(nextBytes.toInt()))
                readBytes.copyInto(destination = buffer, destinationOffset = offset * CIPHER_BLOCK_SIZE_INT)
                remainingBytes -= nextBytes
                bufferFilled += nextBytes.toInt()
                offset++
            }

            if ((size - remainingBytes) > currentStep) {
                currentStep += step
                onProgressUpdate(size - remainingBytes)
            }

            throttle -= BUFFER_SIZE_LONG
            mDataOutput.write(buffer, 0, bufferFilled)

            if (throttle <= 0L) {
                onProgressUpdate(size - remainingBytes)
                throttle = sendBytesPerSecond
                sleep((1000L - ((now() - now).coerceAtLeast(minimumValue = 0))))
                now = now()
            }
        }
        bufferedReadFile.close()
        onProgressUpdate(size)
    }

    open fun sendFile(
        filePath: String,
        encryptBlock: (ByteArray) -> ByteArray,
        progressPrecision: Double,
        onProgressUpdate: (bytes: Long) -> Unit,
    ) {
        val path = Path(filePath)
        val size = FilesObject.size(path)
        sendFile(
            stream = FilesObject.inputStream(filePath),
            name = path.fileName.toString(),
            size = size,
            encryptBlock = encryptBlock,
            progressPrecision = progressPrecision,
            onProgressUpdate = onProgressUpdate,
        )
    }

    open fun receiveFile(
        destination: String,
        decryptBlock: (ByteArray) -> ByteArray,
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
                val buffer = ByteArray(BUFFER_SIZE_INT)
                repeat(times = BUFFER_SIZE_INT / CIPHER_BLOCK_SIZE_INT) { offset ->
                    if (transferByteCount == 0L) {
                        return@repeat
                    }
                    val nextBytes = minOf(transferByteCount, CIPHER_BLOCK_SIZE_LONG)
                    val readBytes = decryptBlock(mDataInput.readNBytesHM(nextBytes.toInt()))
                    readBytes.copyInto(destination = buffer, destinationOffset = offset * CIPHER_BLOCK_SIZE_INT)
                    transferByteCount -= nextBytes
                }
                file.appendBytes(buffer)
            }
        }
        return file.absolutePath to fileName
    }

    open fun receiveFile(
        onOutputStream: (fileName: String) -> OutputStream?,
        decryptBlock: (ByteArray) -> ByteArray,
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
                    val buffer = ByteArray(BUFFER_SIZE_INT)
                    var actual = 0
                    repeat(times = BUFFER_SIZE_INT / CIPHER_BLOCK_SIZE_INT) { offset ->
                        if (transferByteCount == 0L) {
                            return@repeat
                        }
                        val nextBytes = minOf(transferByteCount, CIPHER_BLOCK_SIZE_LONG)
                        val readBytes = decryptBlock(mDataInput.readNBytesHM(nextBytes.toInt()))
                        readBytes.copyInto(destination = buffer, destinationOffset = offset * CIPHER_BLOCK_SIZE_INT)
                        transferByteCount -= nextBytes
                        remainingBytes -= nextBytes
                        actual += nextBytes.toInt()
                    }
                    stream.write(buffer.sliceArray(0..<actual))
                    if ((size - remainingBytes) > currentStep) {
                        currentStep += step
                        onProgressUpdate(size - remainingBytes)
                    }
                }
            }
        }
        onProgressUpdate(size)
    }

    open fun sendContinue() = sendBoolean(message = true)
    open fun receiveContinue() {
        receiveBoolean()
    }

    open fun sendByteArray(message: ByteArray) {
        sendInt(message.size)
        mDataOutput.write(message)
    }

    open fun sendByteArrayRaw(message: ByteArray) {
        mDataOutput.write(message)
    }

    open fun receiveByteArray(): ByteArray {
        val size = receiveInt()
        val buffer = ByteArray(size)
        mDataInput.read(buffer, 0, size)
        return buffer
    }

    open fun receiveByteArrayRaw(size: Int): ByteArray {
        val buffer = ByteArray(size)
        mDataInput.read(buffer, 0, size)
        return buffer
    }
}
