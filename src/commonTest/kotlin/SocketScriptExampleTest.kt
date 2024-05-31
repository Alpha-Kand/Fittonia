import io.mockk.every
import io.mockk.mockkStatic

private class SocketScriptExampleTest : BaseSocketScriptTest() {

    @UnitTest
    fun basicSendReceiveCommunication() = runSocketScriptTest2(
        setupBlock = {},
        {
            val client = generateClient()
            client.sendString("aaa")
            client.sendInt(4)
            client.receiveString()
            client.receiveInt()
        },
        {
            val server = generateServer()
            server.receiveString()
            server.receiveInt()
            server.sendString("bbb")
            server.sendInt(-4)
        },
    )

    @UnitTest
    fun setupBlock() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::mockGenerateKey)
            every { mockGenerateKey() } returns "mockked"
        },
        {
            val client = generateClient(mockGenerateKey())
            client.sendInt(1)
            client.receiveInt()
        },
        {
            val server = generateServer(key = "mockked")
            server.receiveInt()
            server.sendInt(2)
        },
    )

    @UnitTest
    fun doubleSendReceiveCommunication() = runSocketScriptTest2(
        setupBlock = {},
        {
            val client1 = generateClient(key = "connection1")
            client1.sendInt(1)
            client1.receiveInt()
        },
        {
            val client2 = generateClient(key = "connection2")
            client2.sendInt(2)
            client2.receiveInt()
        },
        {
            val server1 = generateServer(key = "connection1")
            server1.receiveInt()
            server1.sendInt(1)
        },
        {
            val server2 = generateServer(key = "connection2")
            server2.receiveInt()
            server2.sendInt(2)
        },
    )
}

private fun mockGenerateKey() = "wrong"
