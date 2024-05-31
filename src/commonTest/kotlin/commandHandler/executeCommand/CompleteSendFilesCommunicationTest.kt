package commandHandler.executeCommand

import BaseSocketScriptTest
import UnitTest
import commandHandler.FileTransfer
import commandHandler.SendFilesCommand
import commandHandler.executeCommand.sendExecution.foundFileNamesTooLong
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.executeCommand.sendExecution.sendCommandExecution
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
import org.junit.jupiter.api.BeforeEach
import settingsManager.SettingsManager

private class CompleteSendFilesCommunicationTest : BaseSocketScriptTest() {

    private val mockkSendCommand = SendFilesCommand(
        files = listOf("file"),
        job = "job",
    ).also { command ->
        command.addArg("--port", "5555")
        command.addArg("--password", "password")
    }

    @BeforeEach
    fun beforeEach() {
        mockkStatic(::setupSendCommandClient)
        every { setupSendCommandClient(any()) } returns generateClient()

        mockkFittoniaTempFileMockFileLines()

        mockkStatic("commandHandler.executeCommand.sendExecution.SendFilesExecutionClientEngineKt")

        mockkObject(HMeadowSocketServer)
        every {
            HMeadowSocketServer.createServerAnyPort(any(), any(), any())
        } returns generateServer(key = "client parent")

        every { SettingsManager.settingsManager.checkPassword(any()) } returns true
    }

    @UnitTest
    fun completeSendFilesCommunicationNormalPath() = runSocketScriptTest2(
        setupBlock = { },
        {
            // Client
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccc", "ddd/eee/fff"),
                serverDestinationDirLength = 12,
                onItemFound = {},
            )

            sendFilesExecutionClientEngine(
                command = mockkSendCommand,
                parent = generateClient(key = "client parent"),
            )
        },
        {
            // Server
            generateServer().handleCommandServerEngine(mockk(relaxed = true))
        },
        {
            // Client Parent
            sendCommandExecution(command = mockk(relaxed = true), inputTokens = emptyList())
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationCancelPath() = runSocketScriptTest2(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        {
            every { any<HMeadowSocketClient>().foundFileNamesTooLong(any(), any()) } returns FileTransfer.CANCEL
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccc", "ddd/eee/fff"),
                serverDestinationDirLength = 126,
                onItemFound = {},
            )

            sendFilesExecutionClientEngine(
                command = mockkSendCommand,
                parent = generateClient(key = "client parent"),
            )
        },
        {
            generateServer().handleCommandServerEngine(mockk(relaxed = true))
        },
        {
            sendCommandExecution(command = mockk(relaxed = true), inputTokens = emptyList())
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationSkipInvalidPath() = runSocketScriptTest2(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        {
            every { any<HMeadowSocketClient>().foundFileNamesTooLong(any(), any()) } returns FileTransfer.SKIP_INVALID
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccccccccccccccc", "ddd/eee/fff"),
                serverDestinationDirLength = 110,
                onItemFound = {},
            )

            sendFilesExecutionClientEngine(
                command = mockkSendCommand,
                parent = generateClient(key = "client parent"),
            )
        },
        {
            generateServer().handleCommandServerEngine(mockk(relaxed = true))
        },
        {
            sendCommandExecution(command = mockk(relaxed = true), inputTokens = emptyList())
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationCompressEverythingPath() = runSocketScriptTest2(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        {
            val mockkSourceFileListManager = mockk<SourceFileListManager>(relaxed = true)
            every { mockkSourceFileListManager.forEachItem(any()) } just Runs

            every {
                any<HMeadowSocketClient>().foundFileNamesTooLong(any(), any())
            } returns FileTransfer.COMPRESS_EVERYTHING
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns mockkSourceFileListManager

            sendFilesExecutionClientEngine(
                command = mockkSendCommand,
                parent = generateClient("client parent"),
            )
        },
        {
            generateServer().handleCommandServerEngine(mockk(relaxed = true))
        },
        {
            sendCommandExecution(command = mockk(relaxed = true), inputTokens = emptyList())
        },
    )

    @UnitTest
    fun completeSendFilesCommunicationCompressInvalidPath() = runSocketScriptTest2(
        setupBlock = {
            mockkFileOperationsFileExists(exists = true)
        },
        {
            every {
                any<HMeadowSocketClient>().foundFileNamesTooLong(any(), any())
            } returns FileTransfer.COMPRESS_INVALID
            every { any<HMeadowSocketClient>().sendFilesCollecting(any(), any()) } returns SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccccccccccccccc", "ddd/eee/fff"),
                serverDestinationDirLength = 110,
                onItemFound = {},
            )

            sendFilesExecutionClientEngine(
                command = mockkSendCommand,
                parent = generateClient("client parent"),
            )
        },
        {
            generateServer().handleCommandServerEngine(mockk(relaxed = true))
        },
        {
            sendCommandExecution(command = mockk(relaxed = true), inputTokens = emptyList())
        },
    )
}
