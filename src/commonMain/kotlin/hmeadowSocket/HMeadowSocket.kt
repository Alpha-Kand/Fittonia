package hmeadowSocket

import java.io.IOException
import java.lang.Thread.sleep
import java.net.BindException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant

sealed class HMeadowSocket(open val socketInterface: HMeadowSocketInterface) {
    enum class SocketErrorType {
        CLIENT_SETUP,
        SERVER_SETUP,
        COULD_NOT_BIND_SERVER_TO_GIVEN_PORT,
        COULD_NOT_FIND_AVAILABLE_PORT,
    }

    class HMeadowSocketError(
        val errorType: SocketErrorType,
        error: Exception,
    ) : Exception(error.message)

    fun sendInt(message: Int) = socketInterface.sendInt(message)
    fun receiveInt() = socketInterface.receiveInt()

    fun sendLong(message: Long) = socketInterface.sendLong(message)
    fun receiveLong() = socketInterface.receiveLong()

    fun sendBoolean(message: Boolean) = socketInterface.sendBoolean(message)
    fun receiveBoolean() = socketInterface.receiveBoolean()

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

    abstract fun close()
}

class HMeadowSocketServer private constructor(
    private val socket: Socket,
    override val socketInterface: HMeadowSocketInterface = HMeadowSocketInterfaceReal(),
) : HMeadowSocket(socketInterface) {
    init {
        try {
            socketInterface.bindToSocket(socket)
        } catch (e: Exception) {
            throw HMeadowSocketError(errorType = SocketErrorType.SERVER_SETUP, error = e)
        }
    }

    override fun close() {
        socket.close()
    }

    companion object {

        /**
         * Creates a server on the given port or throws an error.
         */
        fun createServer(port: Int): HMeadowSocketServer {
            try {
                return HMeadowSocketServer(socket = ServerSocket(port).accept())
            } catch (e: BindException) {
                throw HMeadowSocketError(errorType = SocketErrorType.COULD_NOT_BIND_SERVER_TO_GIVEN_PORT, e)
            }
        }

        /**
         * Creates a server on or after the given port. Calls [onFindAvailablePort] after a server socket was
         * successfully created, but before it blocks for a client.
         *
         * In the unlikely event no ports are found, throws an error.
         */
        fun createServerAnyPort(
            startingPort: Int,
            onFindAvailablePort: (port: Int) -> Unit = {},
        ): HMeadowSocketServer {
            var validPort = startingPort
            var serverSocket: ServerSocket
            while (true) {
                try {
                    serverSocket = ServerSocket(validPort)
                    break
                } catch (e: BindException) {
                    validPort += 1
                    if (validPort > 65535) { // 65535 is the highest port number possible (2^16)-1.
                        throw HMeadowSocketError(errorType = SocketErrorType.COULD_NOT_FIND_AVAILABLE_PORT, e)
                    }
                }
            }
            onFindAvailablePort(validPort)
            return HMeadowSocketServer(socket = serverSocket.accept())
        }
    }
}

class HMeadowSocketClient(
    ipAddress: InetAddress,
    port: Int,
    timeoutMillis: Long = 0,
    override val socketInterface: HMeadowSocketInterface = HMeadowSocketInterfaceReal(),
) : HMeadowSocket(socketInterface) {
    private val socket: Socket

    init {
        val timeLimit = Instant.now().toEpochMilli() + timeoutMillis
        var trySocket: Socket? = null
        do {
            try {
                trySocket = Socket(ipAddress, port)
                socketInterface.bindToSocket(trySocket)
                break
            } catch (e: IOException) {
                sleep(timeoutMillis / 10)
            }
        } while (Instant.now().toEpochMilli() < timeLimit)
        socket = trySocket ?: throw IllegalStateException() // TODO CLIENT_CONNECTION_TIMEOUT
    }

    override fun close() {
        socket.close()
    }
}
