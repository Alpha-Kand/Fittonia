import hmeadowSocket.HMeadowSocketInterfaceReal
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer

private class HMeadowSocketInterfaceTest : DesktopBaseMockkTest() {
    @UnitTest
    @DisplayName("sendInt & receiveInt")
    fun sendReceiveInt() {
        val (socketInterface, socket) = setup()
        val int = 42

        socketInterface.sendInt(int)
        Assertions.assertTrue(socket.checkSentBytes(int.byteArray))

        socket.prepareToReceive(inputBytes = int.byteArray)
        Assertions.assertEquals(int, socketInterface.receiveInt())
    }

    @UnitTest
    @DisplayName("sendLong & receiveLong")
    fun sendReceiveLong() {
        val (socketInterface, socket) = setup()
        val long = 33L

        socketInterface.sendLong(long)
        Assertions.assertTrue(socket.checkSentBytes(long.byteArray))

        socket.prepareToReceive(inputBytes = long.byteArray)
        Assertions.assertEquals(long, socketInterface.receiveLong())
    }

    @UnitTest
    @DisplayName("sendBoolean & receiveBoolean")
    fun sendReceiveBoolean() {
        val (socketInterface, socket) = setup()
        val boolean = true

        socketInterface.sendBoolean(boolean)
        Assertions.assertTrue(socket.checkSentBytes(boolean.byteArray))

        socket.prepareToReceive(inputBytes = boolean.byteArray)
        Assertions.assertEquals(boolean, socketInterface.receiveBoolean())
    }

    @UnitTest
    @DisplayName("sendString & receiveString")
    fun sendReceiveString() {
        val (socketInterface, socket) = setup()
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
        val (socketInterface, socket) = setup()
        val message = "Hello there"
        val messageBytes = message.length.byteArray + message.byteArray

        socketInterface.sendByteArray(message.byteArray)
        Assertions.assertTrue(socket.checkSentBytes(messageBytes))

        socket.prepareToReceive(inputBytes = messageBytes)
        Assertions.assertTrue(message.byteArray.equalsOther(socketInterface.receiveByteArray()))
    }

    @UnitTest
    fun close() {
        val inputStream = mockk<InputStream>(relaxed = true)
        val outputStream = mockk<OutputStream>(relaxed = true)
        HMeadowSocketInterfaceReal().run {
            bindToSocket { MySocket(inputStream = inputStream, outputStream = outputStream) }
            close()
        }
        verify(exactly = 1) {
            inputStream.close()
            outputStream.close()
        }
    }
}

private fun setup(): Pair<HMeadowSocketInterfaceReal, MySocket> {
    val socketInterface = HMeadowSocketInterfaceReal()
    val socket = MySocket()
    socketInterface.bindToSocket { socket }
    return socketInterface to socket
}

private fun MySocket.prepareToReceive(inputBytes: ByteArray) {
    (this.inputStream as? MyInputStream)?.setBuffer(inputBytes) ?: throw Exception()
}

private fun MySocket.checkSentBytes(outputByteArray: ByteArray): Boolean {
    return (this.outputStream as? MyOutputStream)?.getByteArray()?.equalsOther(outputByteArray) ?: throw Exception()
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

private class MyInputStream : InputStream() {
    private var input = ByteArray(1)
    private var index = -1

    fun setBuffer(buffer: ByteArray) {
        input = buffer
        index = 0
    }

    override fun read(): Int {
        return input[index++].toInt()
    }
}

private class MyOutputStream : OutputStream() {
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

private class MySocket(
    private val inputStream: InputStream = MyInputStream(),
    private val outputStream: OutputStream = MyOutputStream(),
) : Socket() {
    override fun getInputStream(): InputStream {
        return inputStream
    }

    override fun getOutputStream(): OutputStream {
        return outputStream
    }
}
