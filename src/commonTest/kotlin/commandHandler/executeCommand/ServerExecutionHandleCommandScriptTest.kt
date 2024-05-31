package commandHandler.executeCommand

import BaseSocketScriptTest
import UnitTest
import commandHandler.AddCommand
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.canContinueSendCommand
import hmeadowSocket.HMeadowSocketServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import sendDeny
import settingsManager.SettingsManager

private class ServerClientHandleCommandScriptTest : BaseSocketScriptTest() {

    @BeforeEach
    fun beforeEach() {
        every {
            SettingsManager.settingsManager.findDestination(any())
        } returns SettingsManager.SettingsData.Destination(
            name = "name",
            ip = "ip",
            password = "password",
        )
        every { SettingsManager.settingsManager.checkPassword(any()) } returns true
        every { SettingsManager.settingsManager.getAutoJobName() } returns "jobAutoName"
        every { SettingsManager.settingsManager.settings.dumpPath } returns "dumpPath"
    }

    // Handle Command
    private val onSendFilesCommand: (Boolean) -> Unit = mockk(relaxed = true)
    private val onSendMessageCommand: (Boolean) -> Unit = mockk(relaxed = true)
    private val onAddDestinationCommand: (Boolean) -> Unit = mockk(relaxed = true)
    private val onInvalidCommand: () -> Unit = mockk(relaxed = true)

    private fun HMeadowSocketServer.handleCommandMock() = handleCommand(
        onSendFilesCommand = onSendFilesCommand,
        onSendMessageCommand = onSendMessageCommand,
        onAddDestination = onAddDestinationCommand,
        onInvalidCommand = onInvalidCommand,
    )

    private fun verifyHandleCommand(callback: (Boolean) -> Unit) {
        listOf(
            onSendFilesCommand,
            onSendMessageCommand,
            onAddDestinationCommand,
        ).forEach {
            if (callback == it) {
                verify(exactly = 1) { it(any()) }
            } else {
                verify(exactly = 0) { it(any()) }
            }
        }
        verify(exactly = 0) { onInvalidCommand() }
    }

    @UnitTest
    fun sendFilesCommand() = runSocketScriptTest2(
        setupBlock = {},
        {
            assertTrue(
                canContinueSendCommand(
                    command = SendFilesCommand(
                        files = listOf("file"),
                        job = "job",
                    ),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                ),
            )
        },
        {
            generateServer().handleCommandMock()
            verifyHandleCommand(onSendFilesCommand)
        },
    )

    @UnitTest
    fun sendMessageCommand() = runSocketScriptTest2(
        setupBlock = {},
        {
            assertTrue(
                canContinueSendCommand(
                    command = SendMessageCommand(message = "message"),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                ),
            )
        },
        {
            generateServer().handleCommandMock()
            verifyHandleCommand(onSendMessageCommand)
        },
    )

    @UnitTest
    fun addDestinationCommand() = runSocketScriptTest2(
        setupBlock = {},
        {
            assertTrue(
                canContinueSendCommand(
                    command = AddCommand(),
                    client = generateClient(),
                    parent = generateClient(key = "parent"),
                ),
            )
        },
        {
            generateServer().handleCommandMock()
            verifyHandleCommand(onAddDestinationCommand)
        },
        {
            val parent = generateServer(key = "parent")
            parent.receiveString()
            parent.receiveString()
        },
    )

    @UnitTest
    fun canContinueSendCommandPasswordRefused() = runSocketScriptTest2(
        setupBlock = {},
        {
            assertFalse(
                canContinueSendCommand(
                    command = AddCommand(),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                ),
            )
        },
        {
            every { SettingsManager.settingsManager.checkPassword(any()) } returns false
            generateServer().handleCommandMock()
            verifyHandleCommand(onAddDestinationCommand)
        },
    )

    @UnitTest
    fun canContinueSendCommandFailure() = runSocketScriptTest2(
        setupBlock = {},
        {
            assertFalse(
                canContinueSendCommand(
                    command = AddCommand(),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                ),
            )
        },
        {
            val server = generateServer()
            server.receiveString()
            server.sendDeny()
        },
    )
}
