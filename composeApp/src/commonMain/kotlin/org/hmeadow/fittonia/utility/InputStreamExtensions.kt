package org.hmeadow.fittonia.utility

import java.io.IOException
import java.io.InputStream
import java.util.Arrays
import kotlin.math.min

fun InputStream.subDivide(maxBlockSize: Int, expectedStreamSize: Long, block: (ByteArray) -> Unit) {
    var offset = 0L
    while (offset < expectedStreamSize) {
        val nextBlockSize = min(maxBlockSize.toLong(), expectedStreamSize - offset).toInt()
        val nextBlock = this.readNBytesHM(length = nextBlockSize)
        offset += nextBlockSize
        block(nextBlock)
    }
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
fun InputStream.readNBytesHM(length: Int): ByteArray {
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
        val buffer = ByteArray(remainingBytes.coerceAtMost(8192)) // TODO
        // Bytes read this iteration.
        var readBytes = 0
        // Read until EOF, which may be more or less than buffer size.
        while (this.read(
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
