package commandHandler.commands

import BaseSocketScriptTest
import UnitTest
import commandHandler.ServerCommandFlag
import commandHandler.communicateCommand
import commandHandler.executeCommand.handleCommand
import hmeadowSocket.HMeadowSocketServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import settingsManager.SettingsManager

private class SendCommandTest : BaseSocketScriptTest() {

    @Nested
    inner class CommunicateCommand {
        // Communicate Command
        private val onSuccess: () -> Unit = mockk(relaxed = true)
        private val onPasswordRefused: () -> Unit = mockk(relaxed = true)
        private val onFailure: () -> Unit = mockk(relaxed = true)

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

        private fun verifyHandleCommandInvalid() {
            listOf(
                onSendFilesCommand,
                onSendMessageCommand,
                onAddDestinationCommand,
            ).forEach {
                verify(exactly = 0) { it(any()) }
            }
            verify(exactly = 1) { onInvalidCommand() }
        }

        private fun verifyCommunicateCommand(callback: () -> Unit) {
            listOf(
                onSuccess,
                onPasswordRefused,
                onFailure,
            ).forEach {
                if (callback == it) {
                    verify(exactly = 1) { it() }
                } else {
                    verify(exactly = 0) { it() }
                }
            }
        }

        @UnitTest
        fun badCommandFlag() = runSocketScriptTest2(
            setupBlock = {},
            {
                generateClient().communicateCommand(
                    commandFlag = mockk(relaxed = true) {
                        every { ordinal } returns -1
                    },
                    password = "password",
                    onSuccess = onSuccess,
                    onPasswordRefused = onPasswordRefused,
                    onFailure = onFailure,
                )
                verifyCommunicateCommand(onFailure)
            },
            {
                generateServer().handleCommandMock()
                verifyHandleCommandInvalid()
            },
        )

        @UnitTest
        fun passwordRefused() = runSocketScriptTest2(
            setupBlock = {},
            {
                generateClient().communicateCommand(
                    commandFlag = ServerCommandFlag.SEND_FILES,
                    password = "password",
                    onSuccess = onSuccess,
                    onPasswordRefused = onPasswordRefused,
                    onFailure = onFailure,
                )
                verifyCommunicateCommand(onPasswordRefused)
            },
            {
                every { SettingsManager.settingsManager.checkPassword(any()) } returns false
                generateServer().handleCommandMock()
                verifyHandleCommand(onSendFilesCommand)
            },
        )

        @UnitTest
        fun communicateCommandSuccess() = runSocketScriptTest2(
            setupBlock = {},
            {
                generateClient().communicateCommand(
                    commandFlag = ServerCommandFlag.SEND_FILES,
                    password = "password",
                    onSuccess = onSuccess,
                    onPasswordRefused = onPasswordRefused,
                    onFailure = onFailure,
                )
                verifyCommunicateCommand(onSuccess)
            },
            {
                every { SettingsManager.settingsManager.checkPassword(any()) } returns true
                generateServer().handleCommandMock()
                verifyHandleCommand(onSendFilesCommand)
            },
        )
    }
}
