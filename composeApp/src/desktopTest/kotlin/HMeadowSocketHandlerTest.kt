import hmeadowSocket.HMeadowSocketHandler
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer

private class HMeadowSocketHandlerTest : DesktopBaseMockkTest() {
    @BeforeEach
    fun beforeEachSetup() {
        mockkObject(HMeadowSocketHandler.Now)
        every { HMeadowSocketHandler.Now.now() } returns 123

        mockkObject(HMeadowSocketHandler.Sleeper)
        every { HMeadowSocketHandler.Sleeper.sleep(any()) } just Runs
    }

    @UnitTest
    @DisplayName("sendInt & receiveInt")
    fun sendReceiveInt() {
        val (socketInterface, socket) = socketSetup()
        val int = 42

        socketInterface.sendInt(int)
        Assertions.assertTrue(socket.checkSentBytes(int.byteArray))

        socket.prepareToReceive(inputBytes = int.byteArray)
        Assertions.assertEquals(int, socketInterface.receiveInt())
    }

    @UnitTest
    @DisplayName("sendLong & receiveLong")
    fun sendReceiveLong() {
        val (socketInterface, socket) = socketSetup()
        val long = 33L

        socketInterface.sendLong(long)
        Assertions.assertTrue(socket.checkSentBytes(long.byteArray))

        socket.prepareToReceive(inputBytes = long.byteArray)
        Assertions.assertEquals(long, socketInterface.receiveLong())
    }

    @UnitTest
    @DisplayName("sendBoolean & receiveBoolean")
    fun sendReceiveBoolean() {
        val (socketInterface, socket) = socketSetup()
        val boolean = true

        socketInterface.sendBoolean(boolean)
        Assertions.assertTrue(socket.checkSentBytes(boolean.byteArray))

        socket.prepareToReceive(inputBytes = boolean.byteArray)
        Assertions.assertEquals(boolean, socketInterface.receiveBoolean())
    }

    @UnitTest
    @DisplayName("sendString & receiveString")
    fun sendReceiveString() {
        val (socketInterface, socket) = socketSetup()
        val message = "Hello there"
        val messageBytes = message.length.byteArray + message.byteArray

        socketInterface.sendString(message)
        Assertions.assertTrue(socket.checkSentBytes(messageBytes))

        socket.prepareToReceive(inputBytes = messageBytes)
        Assertions.assertEquals(message, socketInterface.receiveString())
    }

    @UnitTest
    @DisplayName("sendByteArray & receiveByteArray")
    fun sendReceiveByteArray() {
        val (socketInterface, socket) = socketSetup()
        val message = "Hello there"
        val messageBytes = message.length.byteArray + message.byteArray

        socketInterface.sendByteArray(message.byteArray)
        Assertions.assertTrue(socket.checkSentBytes(messageBytes))

        socket.prepareToReceive(inputBytes = messageBytes)
        Assertions.assertTrue(message.byteArray.equalsOther(socketInterface.receiveByteArray()))
    }

    @Nested
    inner class SendReceiveFiles {
        private val fileName = "File Name"

        private fun createFileBytes(fileStreamSize: Int): ByteArray {
            val fileBytes = ByteArray(size = fileStreamSize)
            repeat(times = fileStreamSize) { index ->
                fileBytes[index] = (index % 100).toByte()
            }
            return fileBytes
        }

        private fun constructTransferByteArray(fileBytes: ByteArray, size: Int): ByteArray {
            val sizeBytes = size.toLong().byteArray
            val nameSizeBytes = fileName.length.byteArray
            val nameBytes = fileName.byteArray
            return sizeBytes + nameSizeBytes + nameBytes + fileBytes
        }

        @UnitTest
        @DisplayName("`sendFile` should send the correct data.")
        fun sendFileBytes() {
            // Setup
            val (socketInterface, socket) = socketSetup()
            val fileStreamSize = 100
            val fileBytes = createFileBytes(fileStreamSize = fileStreamSize)
            val fileStream = TestInputStream()
            fileStream.setFileBytes(buffer = fileBytes)
            val expected = constructTransferByteArray(fileBytes = fileBytes, size = fileStreamSize)

            // Execute
            socketInterface.sendFile(
                stream = fileStream,
                name = fileName,
                size = fileStreamSize.toLong(),
                encryptBlock = { it },
                progressPrecision = 0.01,
                onProgressUpdate = { },
            )

            // Assert
            val actual = (socket.outputStream as? TestOutputStream)?.getByteArray()
            Assertions.assertArrayEquals(expected, actual)
        }

        @UnitTest
        @DisplayName("`sendFile` should call the progress callback the appropriate amount of times.")
        fun sendFileProgress() {
            // Setup
            val (socketInterface, _) = socketSetup()
            val fileStream = TestInputStream()
            val fileStreamSize = 8192 * 1000
            val fileBytes = createFileBytes(fileStreamSize = fileStreamSize)
            fileStream.setFileBytes(buffer = fileBytes)
            var progress = 0

            // Execute
            socketInterface.sendFile(
                stream = fileStream,
                name = fileName,
                size = fileStreamSize.toLong(),
                encryptBlock = { it },
                progressPrecision = 0.01,
                onProgressUpdate = { progress++ },
            )

            // Assert
            Assertions.assertEquals(100, progress)
        }

        @UnitTest
        @DisplayName("`sendFile` should be able to throttle fast transfer speeds.")
        fun sendFileThrottle() {
            // Setup
            val (socketInterface, _) = socketSetup()
            val fileStream = TestInputStream()
            val fileStreamSize = 8192 * 20
            val fileBytes = createFileBytes(fileStreamSize = fileStreamSize)
            fileStream.setFileBytes(buffer = fileBytes)
            var progress = 0
            socketInterface.sendBytesPerSecond = 8192 * 2

            // Execute
            socketInterface.sendFile(
                stream = fileStream,
                name = fileName,
                size = fileStreamSize.toLong(),
                encryptBlock = { it },
                progressPrecision = 0.1,
                onProgressUpdate = { progress++ },
            )

            // Assert

            // 1. Called at the start of the transfer.
            // 2. Called each time in the throttle block to set the sleep time.
            // 3. Called each time in the throttle block immediately after waking from sleep.
            verify(exactly = 21) { HMeadowSocketHandler.Now.now() }
            // 10 Calls for each normal 10% progress updates, 10 calls for the progress update in the throttle block being
            // called every 2 buffers (also 10% each).
            Assertions.assertEquals(20, progress)
        }

        @UnitTest
        @DisplayName("`sendFile` with an empty or invalid name should throw an error.")
        fun sendFileEmptyName() {
            // TODO - After release.
        }

        @UnitTest
        @DisplayName("`sendFile` given a file path should send files correctly.")
        fun sendFilePath() {
            // Setup
            val (socketInterface, socket) = socketSetup()
            val fileStream = TestInputStream()
            val fileStreamSize = 100
            val fileBytes = createFileBytes(fileStreamSize = fileStreamSize)
            fileStream.setFileBytes(buffer = fileBytes)
            val expected = constructTransferByteArray(fileBytes = fileBytes, size = fileStreamSize)

            mockkObject(HMeadowSocketHandler.FilesObject)
            every { HMeadowSocketHandler.FilesObject.size(any()) } returns 100
            every { HMeadowSocketHandler.FilesObject.inputStream(any()) } returns fileStream

            // Execute
            socketInterface.sendFile(
                filePath = "file/$fileName",
                encryptBlock = { it },
                progressPrecision = 0.01,
                onProgressUpdate = { },
            )

            // Assert
            val actual = (socket.outputStream as? TestOutputStream)?.getByteArray()
            Assertions.assertArrayEquals(expected, actual)
        }

        @UnitTest
        @DisplayName("`receiveFile` should send the correct data.")
        fun receiveFileBytes() {
            // Setup
            val (socketInterface, socket) = socketSetup()
            val fileStream = TestOutputStream()
            val fileStreamSize = 100
            val fileBytes = createFileBytes(fileStreamSize = fileStreamSize)
            val incomingBytes = constructTransferByteArray(fileBytes = fileBytes, size = fileStreamSize)
            socket.prepareToReceive(inputBytes = incomingBytes)

            // Execute
            socketInterface.receiveFile(
                onOutputStream = { fileStream },
                progressPrecision = 0.1,
                beforeDownload = { size, name ->
                    Assertions.assertEquals(100, size)
                    Assertions.assertEquals(fileName, name)
                },
                onProgressUpdate = {},
            )

            // Assert
            Assertions.assertArrayEquals(fileBytes, fileStream.getByteArray())
        }

        @UnitTest
        @DisplayName("`receiveFile` should call the progress callback the appropriate amount of times.")
        fun receiveFileProgress() {
            // Setup
            val (socketInterface, socket) = socketSetup()
            val fileStream = TestOutputStream()
            val fileStreamSize = 8192 * 1000
            val fileBytes = createFileBytes(fileStreamSize = fileStreamSize)
            val incomingBytes = constructTransferByteArray(fileBytes = fileBytes, size = fileStreamSize)
            socket.prepareToReceive(inputBytes = incomingBytes)
            var progress = 0

            // Execute
            socketInterface.receiveFile(
                onOutputStream = { fileStream },
                progressPrecision = 0.01,
                beforeDownload = { _, _ -> },
                onProgressUpdate = { progress++ },
            )

            // Assert
            Assertions.assertEquals(100, progress)
        }

        @UnitTest
        @DisplayName("`receiveFile` should handle receiving a directory.")
        fun receiveFileDirectory() {
            // Setup
            val (socketInterface, socket) = socketSetup()
            val fileStream = TestOutputStream()
            val fileStreamSize = 0
            val incomingBytes = constructTransferByteArray(fileBytes = ByteArray(size = 0), size = fileStreamSize)
            socket.prepareToReceive(inputBytes = incomingBytes)

            // Execute
            socketInterface.receiveFile(
                onOutputStream = { fileStream },
                progressPrecision = 0.1,
                beforeDownload = { size, name ->
                    Assertions.assertEquals(0, size)
                    Assertions.assertEquals(fileName, name)
                },
                onProgressUpdate = {},
            )

            // Assert
            Assertions.assertEquals(0, fileStream.getByteArray().size)
        }
    }

    @UnitTest
    @DisplayName("Closing the interface should close the input/output streams.")
    fun close() {
        val inputStream = mockk<InputStream>(relaxed = true)
        val outputStream = mockk<OutputStream>(relaxed = true)
        HMeadowSocketHandler().run {
            bindToSocket { TestSocket(inputStream = inputStream, outputStream = outputStream) }
            close()
        }
        verify(exactly = 1) {
            inputStream.close()
            outputStream.close()
        }
    }
}

private fun socketSetup(): Pair<HMeadowSocketHandler, TestSocket> {
    val socket = TestSocket()
    val socketInterface = HMeadowSocketHandler()
    socketInterface.bindToSocket { socket }
    return socketInterface to socket
}

private fun TestSocket.prepareToReceive(inputBytes: ByteArray) {
    (this.inputStream as? TestInputStream)?.setBuffer(inputBytes) ?: throw Exception()
}

private fun TestSocket.checkSentBytes(outputByteArray: ByteArray): Boolean {
    return (this.outputStream as? TestOutputStream)?.getByteArray()?.equalsOther(outputByteArray) ?: throw Exception()
}

private val Int.byteArray: ByteArray
    get() = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

private val Long.byteArray: ByteArray
    get() = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(this).array()

private val Boolean.byteArray: ByteArray
    get() = ByteBuffer.allocate(1).put(
        if (this) {
            1.toByte()
        } else {
            0.toByte()
        },
    ).array()

private val String.byteArray: ByteArray
    get() = this.encodeToByteArray()

private fun ByteArray.equalsOther(other: ByteArray): Boolean {
    if (this.size != other.size) return false
    forEachIndexed { index, byte ->
        if (other[index] != byte) return false
    }
    return true
}

private class TestInputStream : InputStream() {
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

private class TestOutputStream : OutputStream() {
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

private class TestSocket(
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
