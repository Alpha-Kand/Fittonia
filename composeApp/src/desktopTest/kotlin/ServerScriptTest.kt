import hmeadowSocket.HMeadowSocketServer
import io.mockk.mockk
import io.mockk.verify

private class ServerScriptTest : BaseSocketScriptTest() {

    private val onConfirm: () -> Boolean = mockk(relaxed = true)
    private val onDeny: () -> Boolean = mockk(relaxed = true)

    class TestServer(testScope: BaseSocketScriptTest) : Server {
        val client = testScope.generateClient()
        val server = testScope.generateServer()
        override var jobId: Int
            get() = 0
            set(_) {}

        override fun HMeadowSocketServer.passwordIsValid(): Boolean {
            return true
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
    fun sendReceiveApprovalConfirm() {
        val testServer = TestServer(testScope = this)
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
    fun sendReceiveApprovalDeny() {
        val testServer = TestServer(testScope = this)
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
