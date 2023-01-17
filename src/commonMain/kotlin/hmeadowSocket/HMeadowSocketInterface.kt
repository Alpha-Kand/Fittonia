package hmeadowSocket

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

interface HMeadowSocketInterface {
    fun bindToSocket(socket: Socket) {}

    fun sendInt(message: Int)
    fun receiveInt(): Int

    fun sendLong(message: Long)
    fun receiveLong(): Long

    fun sendFile(path: String, rename: String = "")
    fun receiveFile(destination: String): String

    fun sendString(message: String)
    fun receiveString(): String
}

class HMeadowSocketInterfaceReal : HMeadowSocketInterface {

    lateinit var mDataInput: DataInputStream
    lateinit var mDataOutput: DataOutputStream

    override fun bindToSocket(socket: Socket) {
        mDataInput = DataInputStream(socket.getInputStream())
        mDataOutput = DataOutputStream(socket.getOutputStream())
    }

    override fun sendInt(message: Int) = mDataOutput.writeInt(message)
    override fun receiveInt() = mDataInput.readInt()

    override fun sendLong(message: Long) = mDataOutput.writeLong(message)
    override fun receiveLong() = mDataInput.readLong()

    override fun sendFile(path: String, rename: String) {}
    override fun receiveFile(destination: String): String { return "" }

    override fun sendString(message: String) {}
    override fun receiveString(): String { return "" }
}

class HMeadowSocketInterfaceTest : HMeadowSocketInterface {

    override fun bindToSocket(socket: Socket) {}

    override fun sendInt(message: Int) {}
    override fun receiveInt() = 4

    override fun sendLong(message: Long) {}
    override fun receiveLong() = 5L

    override fun sendFile(path: String, rename: String) {}
    override fun receiveFile(destination: String): String { return "" }

    override fun sendString(message: String) {}
    override fun receiveString(): String { return "" }
}