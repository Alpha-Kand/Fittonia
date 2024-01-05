import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketInterfaceTest
import hmeadowSocket.HMeadowSocketServer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

abstract class BaseSocketScriptTest : BaseMockkTest() {

    private enum class TestFlags(val value: Int) {
        SEND_INT(value = 100),
        RECEIVE_INT(value = -100),

        SEND_STRING(value = 101),
        RECEIVE_STRING(value = -101),
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

    fun runSocketScriptTest(testBlock: suspend TestScope.() -> Unit) = runTest {
        try {
            testBlock()
        } catch (e: Exception) {
            println(e)
        } finally {
            var k = 0
            clientList.zip(serverList) { a, b ->
                println("$k. Client.${a.flag.name} - Server.${b.flag.name}")
                k++
            }
            Assertions.assertEquals(clientList.size, serverList.size)
            clientList.zip(serverList) { a, b ->
                Assertions.assertEquals(a.flag.value, -(b.flag.value))
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun launchSockets(
        clientBlock: () -> Unit,
        serverBlock: () -> Unit,
    ) {
        val clientThread = GlobalScope.launch { clientBlock() }
        clientThread.start()

        val serverThread = GlobalScope.launch { serverBlock() }
        serverThread.start()

        clientThread.join()
        serverThread.join()
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
    ) = object : HMeadowSocketInterfaceTest() {
        override fun receiveInt(): Int {
            return otherQueue.poll(1000, TimeUnit.MILLISECONDS)?.run {
                if (flag == TestFlags.SEND_INT) {
                    thisList.add(this.copy(flag = TestFlags.RECEIVE_INT))
                    value.toInt()
                } else {
                    throw Exception()
                }
            } ?: throw Exception()
        }

        override fun sendInt(message: Int) {
            Communication(flag = TestFlags.SEND_INT, value = message.toString()).let {
                thisQueue.add(it)
                thisList.add(it)
            }
        }

        override fun receiveString(): String {
            return otherQueue.poll(1000, TimeUnit.MILLISECONDS)?.run {
                if (flag == TestFlags.SEND_STRING) {
                    thisList.add(this.copy(flag = TestFlags.RECEIVE_STRING))
                    value
                } else {
                    throw Exception()
                }
            } ?: throw Exception()
        }

        override fun sendString(message: String) {
            Communication(flag = TestFlags.SEND_STRING, value = message).let {
                thisQueue.add(it)
                thisList.add(it)
            }
        }
    }
}
