import BaseSocketScriptTest.TestFlags.Companion.opposite
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketHandler
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer
import org.junit.jupiter.api.BeforeEach
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

abstract class BaseSocketScriptTest : DesktopBaseMockkTest() {

    private enum class TestFlags(val value: Int) {
        SEND_INT(value = 100),
        RECEIVE_INT(value = -100),

        SEND_STRING(value = 101),
        RECEIVE_STRING(value = -101),

        SEND_BOOLEAN(value = 102),
        RECEIVE_BOOLEAN(value = -102),

        SEND_FILE(value = 103),
        RECEIVE_FILE(value = -103),

        SEND_CONTINUE(value = 104),
        RECEIVE_CONTINUE(value = -104),

        SEND_LONG(value = 105),
        RECEIVE_LONG(value = -105),

        DEBUG_CHECKPOINT(value = 106),
        ;

        companion object {
            fun TestFlags.opposite(): TestFlags {
                return TestFlags.entries.first {
                    it.value == this.value * -1
                }
            }
        }
    }

    private data class Communication(val flag: TestFlags, val value: String)

    private val clientQueues = mutableMapOf<String, LinkedBlockingQueue<Communication>>()
    private val serverQueues = mutableMapOf<String, LinkedBlockingQueue<Communication>>()
    private val clientLists = mutableMapOf<String, MutableList<Communication>>()
    private val serverLists = mutableMapOf<String, MutableList<Communication>>()

    @BeforeEach
    fun beforeEachSocketScript() {
        DesktopServer.init(port = 0)
        clientQueues.clear()
        serverQueues.clear()
        clientLists.clear()
        serverLists.clear()
    }

    @OptIn(DelicateCoroutinesApi::class) // These are tests so using 'GlobalScope' is fairly safe.
    fun runSocketScriptTest2(
        setupBlock: suspend TestScope.() -> Unit = {},
        vararg testBlocks: suspend TestScope.() -> Unit,
    ) {
        var throwException: Throwable? = null
        try {
            val handler = CoroutineExceptionHandler { _, exception ->
                throwException = exception
            }
            runTest(timeout = 10.seconds) {
                setupBlock()
                joinAll(
                    *testBlocks.map { block ->
                        GlobalScope.launch(handler) { block() }
                    }.toTypedArray(),
                )
            }
        } finally {
            println()
            print("Clients: ${clientLists.size} -> ")
            clientLists.forEach {
                print("${it.key} [${it.value.size}], ")
            }
            println()
            print("Servers: ${serverLists.size} -> ")
            serverLists.forEach {
                print("${it.key} [${it.value.size}], ")
            }
            println()

            clientLists.toList().map { it.first }.forEach { key ->
                val clientList = clientLists[key].orEmpty()
                val serverList = serverLists[key].orEmpty()

                println("Key (\"$key\")")

                var k = 0
                if (clientList.isEmpty() || serverList.isEmpty()) {
                    clientList.forEach {
                        println("$k. Client.${it.flag.name} = \"${it.value}\"")
                        k++
                    }
                    serverList.forEach {
                        println("$k. Server.${it.flag.name} = \"${it.value}\"")
                        k++
                    }
                } else {
                    var clientIndex = 0
                    var serverIndex = 0
                    var index = 1

                    while (true) {
                        if (clientIndex == clientList.size && serverIndex == serverList.size) break

                        when {
                            clientIndex == clientList.size && serverIndex < serverList.size -> {
                                val serverCommunication = serverList[serverIndex]
                                print("$index. ")
                                println("Server.${serverCommunication.flag.name} (\"${serverCommunication.value}\")")
                                serverIndex++
                            }

                            serverIndex == serverList.size && clientIndex < clientList.size -> {
                                val clientCommunication = clientList[clientIndex]
                                print("$index. ")
                                println("Client.${clientCommunication.flag.name} (\"${clientCommunication.value}\")")
                                serverIndex++
                            }

                            clientIndex == serverIndex -> {
                                val clientCommunication = clientList[clientIndex]
                                val serverCommunication = serverList[serverIndex]
                                print("$index. ")
                                val reportLine = "Client.%1\$s (%2\$s) - Server.%3\$s (%4\$s)"
                                println(
                                    reportLine.format(
                                        clientCommunication.flag.name,
                                        clientCommunication.value,
                                        serverCommunication.flag.name,
                                        serverCommunication.value,
                                    ),
                                )
                                clientIndex++
                                serverIndex++
                            }
                        }
                        index++
                    }
                    if (clientIndex != serverIndex) {
                        throw IllegalStateException("Unbalanced Client/Server communication.")
                    }
                }
                println()
            }

            throwException?.let { throw it }
        }
    }

    private val lock = Mutex()

    suspend fun generateClient(key: String = "default") = lock.withLock {
        HMeadowSocketClient(
            ipAddress = "localhost",
            port = 0,
            handshakeTimeoutMillis = 0,
            socketInterface = generateSocketInterface(
                otherQueue = serverQueues.getOrPut(key) { LinkedBlockingQueue<Communication>() },
                thisQueue = clientQueues.getOrPut(key) { LinkedBlockingQueue<Communication>() },
                thisList = clientLists.getOrPut(key) { mutableListOf() },
            ),
        )
    }

    suspend fun generateServer(key: String = "default") = lock.withLock {
        HMeadowSocketServer(
            socket = Socket(),
            socketInterface = generateSocketInterface(
                otherQueue = clientQueues.getOrPut(key) { LinkedBlockingQueue<Communication>() },
                thisQueue = serverQueues.getOrPut(key) { LinkedBlockingQueue<Communication>() },
                thisList = serverLists.getOrPut(key) { mutableListOf() },
            ),
        )
    }

    private fun generateSocketInterface(
        otherQueue: LinkedBlockingQueue<Communication>,
        thisQueue: LinkedBlockingQueue<Communication>,
        thisList: MutableList<Communication>,
    ) = object : HMeadowSocketHandler() {
        override var sendBytesPerSecond: Long
            get() = 2000
            set(_) {}
        override var receiveBytesPerSecond: Long
            get() = 2000
            set(_) {}

        override fun bindToSocket(block: () -> Socket) = Socket()
        override fun close() {}

        override fun receiveInt() = receive(flag = TestFlags.RECEIVE_INT) { it.toInt() }
        override fun sendInt(message: Int) = send(flag = TestFlags.SEND_INT, message = message.toString())

        override fun receiveLong() = receive(flag = TestFlags.RECEIVE_LONG) { it.toLong() }
        override fun sendLong(message: Long) = send(flag = TestFlags.SEND_LONG, message = message.toString())

        override fun receiveString() = receive(flag = TestFlags.RECEIVE_STRING) { it }
        override fun sendString(message: String) = send(flag = TestFlags.SEND_STRING, message = message)

        override fun receiveContinue() {
            receive(flag = TestFlags.RECEIVE_CONTINUE) { it }
        }

        override fun sendByteArray(message: ByteArray) {
            TODO("Not yet implemented") // After release
        }

        override fun receiveByteArray(): ByteArray {
            TODO("Not yet implemented") // After release
        }

        override fun sendContinue() = send(flag = TestFlags.SEND_CONTINUE, message = "continue")

        override fun receiveBoolean() = receive(flag = TestFlags.RECEIVE_BOOLEAN) { it.toBoolean() }
        override fun sendFile(
            stream: InputStream,
            name: String,
            size: Long,
            progressPrecision: Double,
            onProgressUpdate: (bytes: Long) -> Unit,
        ) {
            TODO("Not yet implemented") // After release
        }

        override fun sendBoolean(message: Boolean) = send(flag = TestFlags.SEND_BOOLEAN, message = message.toString())

        override fun receiveFile(
            destination: String,
            prefix: String,
            suffix: String,
        ): Pair<String, String> = receive(flag = TestFlags.RECEIVE_FILE) { "absolutePath" to "fileName" }

        override fun receiveFile(
            onOutputStream: (fileName: String) -> OutputStream?,
            progressPrecision: Double,
            beforeDownload: (totalBytes: Long, name: String) -> Unit,
            onProgressUpdate: (progress: Long) -> Unit,
        ) {
            TODO("Not yet implemented") // After release
        }

        override fun sendFile(
            filePath: String,
            progressPrecision: Double,
            onProgressUpdate: (bytes: Long) -> Unit,
        ) = send(flag = TestFlags.SEND_FILE, message = filePath)

        /*
        // TODO - After release
        override fun debugCheckpoint() {
            thisList.add(Communication(flag = TestFlags.DEBUG_CHECKPOINT, value = "\uD83C\uDFF4\u200D☠\uFE0F"))
        }
        */

        private fun send(flag: TestFlags, message: String) {
            Communication(flag = flag, value = message.filter { it != '\n' }).let {
                thisQueue.add(it)
                thisList.add(it)
            }
        }

        private fun <T> receive(flag: TestFlags, thingReceived: (String) -> T): T {
            return otherQueue.poll(1000, TimeUnit.MILLISECONDS)?.let { communication ->
                if (communication.flag == flag.opposite()) {
                    thisList.add(communication.copy(flag = flag))
                    thingReceived(communication.value)
                } else {
                    throw Exception()
                }
            } ?: run {
                thisList.add(Communication(flag = flag, value = ""))
                thingReceived("")
                throw Exception()
            }
        }
    }
}
