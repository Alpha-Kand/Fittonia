package hmeadowSocket

import hmeadowSocket.HMeadowSocket.HMeadowSocketError.ClientSetupException
import hmeadowSocket.HMeadowSocket.HMeadowSocketError.CouldNotBindServerToGivenPort
import hmeadowSocket.HMeadowSocket.HMeadowSocketError.CouldNotFindAvailablePort
import hmeadowSocket.HMeadowSocket.HMeadowSocketError.FailedToReceiveException
import hmeadowSocket.HMeadowSocket.HMeadowSocketError.FailedToSendException
import hmeadowSocket.HMeadowSocket.HMeadowSocketError.ServerSetupException
import org.hmeadow.fittonia.utility.debug
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant

sealed class HMeadowSocket(open val socketInterface: HMeadowSocketHandler) {

    sealed class HMeadowSocketError(
        error: Exception,
    ) : Exception(error.message) {
        abstract val hmMessage: String?

        class ClientSetupException(error: Exception) : HMeadowSocketError(error = error) {
            override val hmMessage: String = "There was an error setting up CLIENT"
        }

        class ServerSetupException(error: Exception) : HMeadowSocketError(error = error) {
            override val hmMessage: String = "There was an error setting up SERVER"
        }

        class CouldNotBindServerToGivenPort(error: Exception) : HMeadowSocketError(error = error) {
            override val hmMessage: String = "Could not create server on given port."
        }

        class CouldNotFindAvailablePort(error: Exception) : HMeadowSocketError(error = error) {
            override val hmMessage: String = "Could not find any available ports."
        }

        class FailedToReceiveException(error: Exception) : HMeadowSocketError(error = error) {
            override val hmMessage: String =
                "Failed to receive data. Other side of connection might have closed unexpectedly."
        }

        class FailedToSendException(error: Exception) : HMeadowSocketError(error = error) {
            override val hmMessage: String = "Failed to send data."
        }
    }

    private val history = mutableListOf<String>()

    @Throws(FailedToReceiveException::class)
    private fun <T> receiveErrorWrapper(receive: () -> T): T {
        try {
            return receive()
        } catch (e: Exception) {
            this.close()
            debug {
                println(e.message)
                history.forEach { println(it) }
            }
            throw FailedToReceiveException(e)
        }
    }

    @Throws(FailedToSendException::class)
    private fun sendErrorWrapper(send: () -> Unit) {
        try {
            send()
        } catch (e: Exception) {
            this.close()
            debug {
                println(e.message)
                history.forEach { println(it) }
            }
            throw FailedToSendException(e)
        }
    }

    var sendBytesPerSecond: Long
        get() = socketInterface.sendBytesPerSecond
        set(value) {
            socketInterface.sendBytesPerSecond = value
        }
    var receiveBytesPerSecond: Long
        get() = throw Exception() // TODO socketInterface.receiveBytesPerSecond - After release
        set(value) {
            socketInterface.receiveBytesPerSecond = value
        }

    fun sendInt(message: Int) = sendErrorWrapper {
        socketInterface.sendInt(message)
        history.add("SendInt -> $message")
    }

    fun receiveInt() = receiveErrorWrapper { socketInterface.receiveInt() }

    fun sendLong(message: Long) = sendErrorWrapper {
        socketInterface.sendLong(message)
        history.add("SendLong -> $message")
    }

    fun receiveLong() = receiveErrorWrapper { socketInterface.receiveLong() }

    fun sendBoolean(message: Boolean) = sendErrorWrapper {
        socketInterface.sendBoolean(message)
        history.add("SendBoolean -> $message")
    }

    fun receiveBoolean() = receiveErrorWrapper {
        socketInterface.receiveBoolean().also { history.add("ReceiveBoolean -> $it") }
    }

    fun sendString(message: String) = sendErrorWrapper {
        socketInterface.sendString(message)
        history.add("SendString -> $message")
    }

    fun receiveString() = receiveErrorWrapper {
        socketInterface.receiveString().also { history.add("ReceiveString -> $it") }
    }

    fun sendFile(
        stream: InputStream,
        name: String,
        size: Long,
        encryptBlock: (ByteArray) -> ByteArray = { it },
        progressPrecision: Double = 0.01,
        onProgressUpdate: (bytes: Long) -> Unit = {},
    ) = sendErrorWrapper {
        socketInterface.sendFile(
            stream = stream,
            name = name,
            size = size,
            encryptBlock = encryptBlock,
            progressPrecision = progressPrecision,
            onProgressUpdate = onProgressUpdate,
        )
        history.add("SendFile -> $name")
    }

    fun sendFile(
        filePath: String,
        encryptBlock: (ByteArray) -> ByteArray = { it },
        progressPrecision: Double = 0.01,
        onProgressUpdate: (bytes: Long) -> Unit = {},
    ) = sendErrorWrapper {
        socketInterface.sendFile(
            filePath = filePath,
            encryptBlock = encryptBlock,
            progressPrecision = progressPrecision,
            onProgressUpdate = onProgressUpdate,
        )
        history.add("SendFile -> $filePath")
    }

    fun receiveFile(
        destination: String = "",
        decryptBlock: (ByteArray) -> ByteArray = { it },
        prefix: String = "___",
        suffix: String = "___",
    ): Pair<String, String> = receiveErrorWrapper {
        socketInterface.receiveFile(
            destination = destination,
            decryptBlock = decryptBlock,
            prefix = prefix,
            suffix = suffix,
        ).also { history.add("ReceiveFile -> $it") }
    }

    fun receiveFile(
        onOutputStream: (fileName: String) -> OutputStream?,
        decryptBlock: (ByteArray) -> ByteArray = { it },
        progressPrecision: Double = 0.01,
        beforeDownload: (totalBytes: Long, name: String) -> Unit = { _, _ -> },
        onProgressUpdate: (progress: Long) -> Unit = {},
    ) = receiveErrorWrapper {
        socketInterface.receiveFile(
            onOutputStream = onOutputStream,
            decryptBlock = decryptBlock,
            progressPrecision = progressPrecision,
            beforeDownload = beforeDownload,
            onProgressUpdate = onProgressUpdate,
        ).also { history.add("ReceiveFile -> $it") }
    }

    fun sendContinue() = sendErrorWrapper {
        socketInterface.sendContinue()
        history.add("SendContinue")
    }

    fun receiveContinue() = receiveErrorWrapper {
        socketInterface.receiveContinue().also { history.add("ReceiveContinue") }
    }

    fun sendByteArray(message: ByteArray) = sendErrorWrapper {
        socketInterface.sendByteArray(message = message)
    }

    fun receiveByteArray() = receiveErrorWrapper {
        socketInterface.receiveByteArray()
    }

    abstract fun close()

    abstract fun sendClose()

    open fun closeSocket(socket: Socket) {
        try {
            sendInt(-1)
            receiveInt()
        } catch (e: FailedToReceiveException) {
            // Expected, trying to shutdown anyway.
        } finally {
            socket.close()
        }
    }
}

open class HMeadowSocketServer(
    private val socket: Socket,
    operationTimeoutMillis: Int = 0,
    final override val socketInterface: HMeadowSocketHandler = HMeadowSocketHandler(),
) : HMeadowSocket(socketInterface) {
    init {
        try {
            socketInterface.bindToSocket { socket }
        } catch (e: Exception) {
            throw ServerSetupException(e)
        }
        socket.soTimeout = operationTimeoutMillis
    }

    override fun close() {
        socketInterface.close()
        socket.close()
    }

    override fun sendClose() = closeSocket(socket = socket)

    companion object {

        fun createServerFromSocket(
            serverSocket: ServerSocket,
            socketInterface: HMeadowSocketHandler = HMeadowSocketHandler(),
        ): HMeadowSocketServer {
            return HMeadowSocketServer(
                socket = serverSocket.accept(),
                socketInterface = socketInterface,
            )
        }

        /**
         * Creates a server on the given port or throws an error.
         */
        @Throws(CouldNotBindServerToGivenPort::class, ServerSetupException::class)
        fun createServer(
            port: Int,
            timeoutMillis: Long = 0,
            socketInterface: HMeadowSocketHandler = HMeadowSocketHandler(),
        ): HMeadowSocketServer {
            val timeLimit = Instant.now().toEpochMilli() + timeoutMillis
            var exception: Exception
            do {
                try {
                    val serverSocket = ServerSocket(port)
                    val hmeadowSocketServer = HMeadowSocketServer(
                        socket = serverSocket.accept(),
                        socketInterface = socketInterface,
                    )
                    serverSocket.close()
                    return hmeadowSocketServer
                } catch (e: IOException) {
                    exception = e
                    sleep(timeoutMillis / 10)
                }
            } while (Instant.now().toEpochMilli() < timeLimit)

            try {
                throw exception
            } catch (e: ServerSetupException) {
                throw e
            } catch (e: Exception) {
                throw CouldNotBindServerToGivenPort(e)
            }
        }

        /**
         * Creates a server socket on the given port or throws an error.
         */
        @Throws(CouldNotBindServerToGivenPort::class)
        fun createServerSocket(port: Int): ServerSocket {
            try {
                return ServerSocket(port)
            } catch (e: Exception) {
                throw CouldNotBindServerToGivenPort(e)
            }
        }

        /**
         * Creates a server on or after the given port. Calls [onFindAvailablePort] after a server socket was
         * successfully created, but before it blocks for a client.
         *
         * In the unlikely event no ports are found, throws an error.
         */
        @Throws(CouldNotFindAvailablePort::class, ServerSetupException::class)
        fun createServerAnyPort(
            startingPort: Int,
            socketInterface: HMeadowSocketHandler = HMeadowSocketHandler(),
            onFindAvailablePort: (port: Int) -> Unit = {},
        ): HMeadowSocketServer {
            return HMeadowSocketServer(
                socket = createServerSocketAnyPort(
                    startingPort = startingPort,
                    onFindAvailablePort = onFindAvailablePort,
                ),
                socketInterface = socketInterface,
            )
        }

        @Throws(CouldNotFindAvailablePort::class, ServerSetupException::class)
        fun createServerSocketAnyPort(
            startingPort: Int,
            onFindAvailablePort: (port: Int) -> Unit = {},
        ): Socket {
            var validPort = startingPort
            var serverSocket: ServerSocket
            while (true) {
                try {
                    serverSocket = ServerSocket(validPort)
                    break
                } catch (e: Exception) {
                    validPort += 1
                    if (validPort > 65535) { // 65535 is the highest port number possible (2^16)-1.
                        throw CouldNotFindAvailablePort(e)
                    }
                }
            }
            onFindAvailablePort(validPort)
            return serverSocket.accept()
        }
    }
}

open class HMeadowSocketClient
@Throws(ClientSetupException::class)
constructor(
    ipAddress: String,
    port: Int,
    handshakeTimeoutMillis: Int = 0,
    operationTimeoutMillis: Int = 0,
    final override val socketInterface: HMeadowSocketHandler = HMeadowSocketHandler(),
) : HMeadowSocket(socketInterface) {
    private val socket: Socket = socketInterface.bindToSocket {
        val timeLimit = Instant.now().toEpochMilli() + handshakeTimeoutMillis
        var trySocket: Socket? = null
        do {
            try {
                trySocket = Socket().also { it.connect(InetSocketAddress(ipAddress, port), handshakeTimeoutMillis) }
                break
            } catch (e: IOException) {
                sleep(handshakeTimeoutMillis / 10L)
            }
        } while (Instant.now().toEpochMilli() < timeLimit)
        trySocket ?: throw ClientSetupException(Exception())
    }

    init {
        socket.soTimeout = operationTimeoutMillis
    }

    override fun close() {
        socketInterface.close()
        socket.close()
    }

    override fun sendClose() = closeSocket(socket = socket)
}
