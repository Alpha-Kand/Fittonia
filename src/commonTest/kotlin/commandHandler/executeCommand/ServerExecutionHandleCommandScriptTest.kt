package commandHandler.executeCommand

import BaseSocketScriptTest
import UnitTest
import commandHandler.AddCommand
import commandHandler.FileTransfer
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.ServerCommand
import commandHandler.ServerCommandFlag
import commandHandler.canContinueSendCommand
import commandHandler.communicateCommand
import commandHandler.executeCommand.sendExecution.foundFileNamesTooLong
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.executeCommand.sendExecution.sendFilesCollecting
import commandHandler.executeCommand.sendExecution.sendFilesExecutionClientEngine
import commandHandler.setupSendCommandClient
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
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
            password = "password"
        )
        every { SettingsManager.settingsManager.getAutoJobName() } returns "jobAutoName"
        every { SettingsManager.settingsManager.settings.dumpPath } returns "dumpPath"
    }

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
    fun sendFilesCommand() = runSocketScriptTest(
        clientBlock = {
            assertTrue(
                canContinueSendCommand(
                    command = SendFilesCommand(
                        files = listOf("file"),
                        job = "job",
                    ),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                )
            )
        },
        serverBlock = {
            generateServer().handleCommandMock()
            verifyHandleCommand(onSendFilesCommand)
        },
    )

    @UnitTest
    fun sendMessageCommand() = runSocketScriptTest(
        clientBlock = {
            assertTrue(
                canContinueSendCommand(
                    command = SendMessageCommand(message = "message"),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                )
            )
        },
        serverBlock = {
            generateServer().handleCommandMock()
            verifyHandleCommand(onSendMessageCommand)
        },
    )

    @UnitTest
    fun addDestinationCommand() = runSocketScriptTest(
        clientBlock = {
            assertTrue(
                canContinueSendCommand(
                    command = AddCommand(),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                )
            )
        },
        serverBlock = {
            generateServer().handleCommandMock()
            verifyHandleCommand(onAddDestinationCommand)
        },
    )


    @UnitTest
    fun canContinueSendCommandPasswordRefused() = runSocketScriptTest(
        clientBlock = {
            assertFalse(
                canContinueSendCommand(
                    command = AddCommand(),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                )
            )
        },
        serverBlock = {
            every { SettingsManager.settingsManager.checkPassword(any()) } returns false
            generateServer().handleCommandMock()
            verifyHandleCommand(onAddDestinationCommand)
        },
    )

    @UnitTest
    fun canContinueSendCommandFailure() = runSocketScriptTest(
        clientBlock = {
            assertFalse(
                canContinueSendCommand(
                    command = AddCommand(),
                    client = generateClient(),
                    parent = mockk(relaxed = true),
                )
            )
        },
        serverBlock = {
            val server = generateServer()
            server.receiveString()
            server.sendDeny()
        },
    )

    @UnitTest
    fun badCommandFlag() = runSocketScriptTest(
        clientBlock = {
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
        serverBlock = {
            generateServer().handleCommandMock()
            verifyHandleCommandInvalid()
        },
    )

    @UnitTest
    fun passwordRefused() = runSocketScriptTest(
        clientBlock = {
            generateClient().communicateCommand(
                commandFlag = ServerCommandFlag.SEND_FILES,
                password = "password",
                onSuccess = onSuccess,
                onPasswordRefused = onPasswordRefused,
                onFailure = onFailure,
            )
            verifyCommunicateCommand(onPasswordRefused)
        },
        serverBlock = {
            every { SettingsManager.settingsManager.checkPassword(any()) } returns false
            generateServer().handleCommandMock()
            verifyHandleCommand(onSendFilesCommand)
        },
    )

    @UnitTest
    fun communicateCommandSuccess() = runSocketScriptTest(
        clientBlock = {
            generateClient().communicateCommand(
                commandFlag = ServerCommandFlag.SEND_FILES,
                password = "password",
                onSuccess = onSuccess,
                onPasswordRefused = onPasswordRefused,
                onFailure = onFailure,
            )
            verifyCommunicateCommand(onSuccess)
        },
        serverBlock = {
            generateServer().handleCommandMock()
            verifyHandleCommand(onSendFilesCommand)
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationNormalPath() = runSocketScriptTest(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        clientBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()

            mockkFittoniaTempFileMockFileLines()

            mockkStatic("commandHandler.executeCommand.sendExecution.SendFilesExecutionClientEngineKt")
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccc", "ddd/eee/fff"),
                serverDestinationDirLength = 12,
                onItemFound = {},
            )
            every { any<HMeadowSocketClient>().foundFileNamesTooLong(any(), any()) } returns FileTransfer.NORMAL

            sendFilesExecutionClientEngine(
                command = SendFilesCommand(
                    files = listOf("file"),
                    job = "job",
                ).also { it.addArg("--port", "5555") },
                parent = mockk(relaxed = true),
            )
        },
        serverBlock = {
            val server = generateServer()
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServer(any(), any(), any()) } returns server

            mockkStatic("commandHandler.executeCommand.ServerSendFilesExecutionKt")
            every { any<HMeadowSocketServer>().sendFilesServerSetup() } answers {
                server.receiveString()
                server.receiveString()
                server.sendInt(25)
                "job path"
            }

            serverExecution(command = ServerCommand().also { it.addArg("--port", "5555") })
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationCancelPath() = runSocketScriptTest(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        clientBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()

            mockkFittoniaTempFileMockFileLines()

            mockkStatic("commandHandler.executeCommand.sendExecution.SendFilesExecutionClientEngineKt")
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccc", "ddd/eee/fff"),
                serverDestinationDirLength = 126,
                onItemFound = {},
            )
            every { any<HMeadowSocketClient>().foundFileNamesTooLong(any(), any()) } returns FileTransfer.CANCEL

            sendFilesExecutionClientEngine(
                command = SendFilesCommand(
                    files = listOf("file"),
                    job = "job",
                ).also { it.addArg("--port", "5555") },
                parent = mockk(relaxed = true),
            )
        },
        serverBlock = {
            val server = generateServer()
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServer(any(), any(), any()) } returns server

            mockkStatic("commandHandler.executeCommand.ServerSendFilesExecutionKt")
            every { any<HMeadowSocketServer>().sendFilesServerSetup() } answers {
                server.receiveString()
                server.receiveString()
                server.sendInt(25)
                "job path"
            }

            serverExecution(command = ServerCommand().also { it.addArg("--port", "5555") })
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationSkipInvalidPath() = runSocketScriptTest(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        clientBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()

            mockkFittoniaTempFileMockFileLines()

            mockkStatic("commandHandler.executeCommand.sendExecution.SendFilesExecutionClientEngineKt")
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccccccccccccccc", "ddd/eee/fff"),
                serverDestinationDirLength = 110,
                onItemFound = {},
            )
            every { any<HMeadowSocketClient>().foundFileNamesTooLong(any(), any()) } returns FileTransfer.SKIP_INVALID

            sendFilesExecutionClientEngine(
                command = SendFilesCommand(
                    files = listOf("file"),
                    job = "job",
                ).also { it.addArg("--port", "5555") },
                parent = mockk(relaxed = true),
            )
        },
        serverBlock = {
            val server = generateServer()
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServer(any(), any(), any()) } returns server

            mockkStatic("commandHandler.executeCommand.ServerSendFilesExecutionKt")
            every { any<HMeadowSocketServer>().sendFilesServerSetup() } answers {
                server.receiveString()
                server.receiveString()
                server.sendInt(25)
                "job path"
            }

            serverExecution(command = ServerCommand().also { it.addArg("--port", "5555") })
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationCompressEverythingPath() = runSocketScriptTest(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        clientBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()

            mockkFittoniaTempFileMockFileLines()

            val mockkSourceFileListManager = mockk<SourceFileListManager>(relaxed = true)
            every { mockkSourceFileListManager.forEachItem(any()) } just Runs

            mockkStatic("commandHandler.executeCommand.sendExecution.SendFilesExecutionClientEngineKt")
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns mockkSourceFileListManager
            every {
                any<HMeadowSocketClient>().foundFileNamesTooLong(
                    any(),
                    any()
                )
            } returns FileTransfer.COMPRESS_EVERYTHING

            sendFilesExecutionClientEngine(
                command = SendFilesCommand(
                    files = listOf("file"),
                    job = "job",
                ).also { it.addArg("--port", "5555") },
                parent = mockk(relaxed = true),
            )
        },
        serverBlock = {
            val server = generateServer()
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServer(any(), any(), any()) } returns server

            mockkStatic("commandHandler.executeCommand.ServerSendFilesExecutionKt")
            every { any<HMeadowSocketServer>().sendFilesServerSetup() } answers {
                server.receiveString()
                server.receiveString()
                server.sendInt(25)
                "job path"
            }

            serverExecution(command = ServerCommand().also { it.addArg("--port", "5555") })
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationCompressInvalidPath() = runSocketScriptTest(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        clientBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()

            mockkFittoniaTempFileMockFileLines()

            mockkStatic("commandHandler.executeCommand.sendExecution.SendFilesExecutionClientEngineKt")
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccccccccccccccc", "ddd/eee/fff"),
                serverDestinationDirLength = 110,
                onItemFound = {},
            )
            every {
                any<HMeadowSocketClient>().foundFileNamesTooLong(
                    any(),
                    any()
                )
            } returns FileTransfer.COMPRESS_INVALID

            sendFilesExecutionClientEngine(
                command = SendFilesCommand(
                    files = listOf("file"),
                    job = "job",
                ).also { it.addArg("--port", "5555") },
                parent = mockk(relaxed = true),
            )
        },
        serverBlock = {
            val server = generateServer()
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServer(any(), any(), any()) } returns server

            mockkStatic("commandHandler.executeCommand.ServerSendFilesExecutionKt")
            every { any<HMeadowSocketServer>().sendFilesServerSetup() } answers {
                server.receiveString()
                server.receiveString()
                server.sendInt(25)
                "job path"
            }

            serverExecution(command = ServerCommand().also { it.addArg("--port", "5555") })
        },
    )
}