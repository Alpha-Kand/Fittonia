import BaseSocketScriptTest.TestFlags.Companion.opposite
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketInterface
import hmeadowSocket.HMeadowSocketServer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

abstract class BaseSocketScriptTest : BaseMockkTest() {

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

    private val clientQueue = LinkedBlockingQueue<Communication>()
    private val serverQueue = LinkedBlockingQueue<Communication>()
    private val clientList = mutableListOf<Communication>()
    private val serverList = mutableListOf<Communication>()

    @BeforeEach
    fun beforeEachSocketScript() {
        clientQueue.clear()
        serverQueue.clear()
        clientList.clear()
        serverList.clear()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun runSocketScriptTest(
        clientBlock: TestScope.() -> Unit,
        serverBlock: TestScope.() -> Unit,
    ) {
        var throwException: Throwable? = null
        try {
            val handler = CoroutineExceptionHandler { _, exception ->
                throwException = exception
            }
            runTest(timeout = 5.seconds) {
                joinAll(
                    GlobalScope.launch(handler) { clientBlock() },
                    GlobalScope.launch(handler) { serverBlock() },
                )
            }
        } finally {
            throwException?.let { throw it }
            var k = 0
            if (clientList.isEmpty() || serverList.isEmpty()) {
                clientList.forEach {
                    println("$k. Client.${it.flag.name}")
                    k++
                }
                serverList.forEach {
                    println("$k. Server.${it.flag.name}")
                    k++
                }
            } else {
                clientList.zip(serverList) { a, b ->
                    println("$k. Client.${a.flag.name} - Server.${b.flag.name}")
                    k++
                }
                clientList.forEachIndexed { index, communication ->
                    if (index > serverList.size - 1) {
                        println("$index. Client.${communication.flag.name}")
                    }
                }
                serverList.forEachIndexed { index, communication ->
                    if (index > clientList.size - 1) {
                        println("$index. Server.${communication.flag.name}")
                    }
                }
            }
            Assertions.assertEquals(clientList.size, serverList.size)
            clientList.zip(serverList) { a, b ->
                Assertions.assertEquals(a.flag.value, -(b.flag.value))
            }
        }
    }

    fun generateClient() = HMeadowSocketClient(
        ipAddress = InetAddress.getByName("localhost"),
        port = 0,
        timeoutMillis = 0,
        socketInterface = generateSocketInterface(
            otherQueue = serverQueue,
            thisQueue = clientQueue,
            thisList = clientList,
        ),
    )

    fun generateServer() = HMeadowSocketServer(
        socket = Socket(),
        socketInterface = generateSocketInterface(
            otherQueue = clientQueue,
            thisQueue = serverQueue,
            thisList = serverList,
        ),
    )

    private fun generateSocketInterface(
        otherQueue: LinkedBlockingQueue<Communication>,
        thisQueue: LinkedBlockingQueue<Communication>,
        thisList: MutableList<Communication>,
    ) = object : HMeadowSocketInterface {

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

        override fun sendContinue() = send(flag = TestFlags.SEND_CONTINUE, message = "continue")

        override fun receiveBoolean() = receive(flag = TestFlags.RECEIVE_BOOLEAN) { it.toBoolean() }
        override fun sendBoolean(message: Boolean) = send(flag = TestFlags.SEND_BOOLEAN, message = message.toString())

        override fun receiveFile(
            destination: String,
            prefix: String,
            suffix: String,
        ): Pair<String, String> = receive(flag = TestFlags.RECEIVE_FILE) { "absolutePath" to "fileName" }

        override fun sendFile(
            filePath: String,
            rename: String,
        ) = send(flag = TestFlags.SEND_FILE, message = Pair(filePath, rename).toString())

        private fun send(flag: TestFlags, message: String) {
            Communication(flag = flag, value = message).let {
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
            } ?: throw Exception()
        }
    }
}
