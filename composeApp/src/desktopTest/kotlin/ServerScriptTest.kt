import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex

private class ServerScriptTest : BaseSocketScriptTest() {

    private val onConfirm: () -> Boolean = mockk(relaxed = true)
    private val onDeny: () -> Boolean = mockk(relaxed = true)

    suspend fun testServerFactory(): TestServer {
        return TestServer(
            client = generateClient(),
            server = generateServer(),
        )
    }

    class TestServer(
        val client: HMeadowSocketClient,
        val server: HMeadowSocketServer,
    ) : Server {
        override var jobId: Int
            get() = 0
            set(_) {}
        override val jobIdMutex = Mutex()

        override fun HMeadowSocketServer.passwordIsValid(): Boolean {
            return true
        }

        override suspend fun onPing(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
            TODO("Not yet implemented") // After release
        }

        override suspend fun onAddDestination(
            clientPasswordSuccess: Boolean,
            server: HMeadowSocketServer,
            jobId: Int,
        ) {
        }

        override suspend fun onSendFiles(
            clientPasswordSuccess: Boolean,
            server: HMeadowSocketServer,
            jobId: Int,
        ) {
        }

        override suspend fun onSendMessage(
            clientPasswordSuccess: Boolean,
            server: HMeadowSocketServer,
            jobId: Int,
        ) {
        }

        override suspend fun onInvalidCommand(unknownCommand: String) {}
    }

    @UnitTest
    fun sendReceiveApprovalConfirm() = runBlocking {
        val testServer = testServerFactory()
        runSocketScriptTest2(
            setupBlock = {},
            {
                testServer.client.receiveApproval(
                    onConfirm = onConfirm,
                    onDeny = onDeny,
                )
                verify(exactly = 1) { onConfirm() }
                verify(exactly = 0) { onDeny() }
            },
            {
                with(testServer) {
                    testServer.server.sendApproval(choice = true)
                }
            },
        )
    }

    @UnitTest
    fun sendReceiveApprovalDeny() = runBlocking {
        val testServer = testServerFactory()
        runSocketScriptTest2(
            setupBlock = {},
            {
                testServer.client.receiveApproval(
                    onConfirm = onConfirm,
                    onDeny = onDeny,
                )
                verify(exactly = 0) { onConfirm() }
                verify(exactly = 1) { onDeny() }
            },
            {
                with(testServer) {
                    testServer.server.sendApproval(choice = false)
                }
            },
        )
    }
}
