package hmeadowSocket

import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

sealed class HMeadowSocket(open val socketInterface: HMeadowSocketInterface) {
    enum class SocketErrorType {
        CLIENT_SETUP,
        SERVER_SETUP,
    }

    class HMeadowSocketError(
        val errorType: SocketErrorType,
        error: Exception,
    ) : Exception(error.message)

    fun sendInt(message: Int) = socketInterface.sendInt(message)
    fun receiveInt() = socketInterface.receiveInt()

    fun sendLong(message: Long) = socketInterface.sendLong(message)
    fun receiveLong() = socketInterface.receiveLong()

    fun sendString(message: String) = socketInterface.sendString(message)
    fun receiveString() = socketInterface.receiveString()

    fun sendFile(
        filePath: String,
        rename: String = "",
    ) = socketInterface.sendFile(filePath = filePath, rename = rename)

    fun receiveFile(
        destination: String = "",
        prefix: String = "___",
        suffix: String = "___",
    ): Pair<String, String> = socketInterface.receiveFile(
        destination = destination,
        prefix = prefix,
        suffix = suffix,
    )
}

class HMeadowSocketServer(
    port: Int,
    override val socketInterface: HMeadowSocketInterface = HMeadowSocketInterfaceReal(),
) : HMeadowSocket(socketInterface) {
    init {
        try {
            socketInterface.bindToSocket(ServerSocket(port).accept())
        } catch (e: Exception) {
            throw HMeadowSocketError(errorType = SocketErrorType.SERVER_SETUP, error = e)
        }
    }
}

class HMeadowSocketClient(
    ipAddress: InetAddress,
    port: Int,
    override val socketInterface: HMeadowSocketInterface = HMeadowSocketInterfaceReal(),
) : HMeadowSocket(socketInterface) {
    init {
        try {
            socketInterface.bindToSocket(Socket(ipAddress, port))
        } catch (e: Exception) {
            throw HMeadowSocketError(errorType = SocketErrorType.CLIENT_SETUP, error = e)
        }
    }
}
