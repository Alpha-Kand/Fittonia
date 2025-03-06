package commandHandler.executeCommand

import BaseSocketScriptTest
import DesktopServer
import SettingsManagerDesktop
import UnitTest
import commandHandler.FileTransfer
import commandHandler.FileTransfer.Companion.toName
import commandHandler.SendFilesCommand
import commandHandler.executeCommand.sendExecution.fileNamesTooLong
import commandHandler.executeCommand.sendExecution.helpers.FileZipper
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.executeCommand.sendExecution.sendFilesCollecting
import commandHandler.executeCommand.sendExecution.sendFilesExecution
import commandHandler.ipArguments
import commandHandler.passwordArguments
import commandHandler.portArguments
import commandHandler.setupSendCommandClient
import fileOperations.FileOperations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path

private class SendFilesScriptTest : BaseSocketScriptTest() {
    val mockTransferFiles = listOf("/aaa/bbb/ccc", "ddd/eee/fff")

    private fun mockSourceFileListManager(length: Int = 126) = SourceFileListManager(
        userInputPaths = mockTransferFiles,
        serverDestinationDirLength = length,
        onItemFound = {},
    )

    @BeforeEach
    fun setup() {
        every { SettingsManagerDesktop.settingsManager.checkPassword(any()) } returns true
        every { SettingsManagerDesktop.settingsManager.settings.dumpPath } returns "dump path"
        coEvery { SettingsManagerDesktop.settingsManager.getAutoJobName() } returns "job name"

        mockkStatic("commandHandler.executeCommand.sendExecution.SendFilesExecutionKt")

        mockkFittoniaTempFileMockFileLines()
        every { FileOperations.exists(any()) }.answers {
            (it.invocation.args[0] as Path).toString() in mockTransferFiles
        }

        mockkConstructor(FileZipper::class)
        every { anyConstructed<FileZipper>().zipItem(any()) } just Runs
        every { anyConstructed<FileZipper>().finalize(any()) } answers {
            this.firstArg<(String) -> Unit>().invoke("")
        }
    }

    @UnitTest
    fun sendFilesNormal() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()
            coEvery { fileNamesTooLong(any(), any()) } returns FileTransfer.NORMAL.toName
            every { sendFilesCollecting(any(), any()) } returns mockSourceFileListManager()
        },
        {
            sendFilesExecution(
                command = SendFilesCommand().also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = passwordArguments.first(), value = "password")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                    it.setFiles(listOf("file1", "file2"))
                },
            )
        },
        {
            DesktopServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )

    @UnitTest
    fun sendFilesCancel() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()
            coEvery { fileNamesTooLong(any(), any()) } returns FileTransfer.CANCEL.toName
            every { sendFilesCollecting(any(), any()) } returns mockSourceFileListManager()
        },
        {
            sendFilesExecution(
                command = SendFilesCommand().also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = passwordArguments.first(), value = "password")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                    it.setFiles(listOf("file1", "file2"))
                },
            )
        },
        {
            DesktopServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )

    @UnitTest
    fun sendFilesSkipInvalid() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()
            coEvery { fileNamesTooLong(any(), any()) } returns FileTransfer.SKIP_INVALID.toName
            every { sendFilesCollecting(any(), any()) } returns mockSourceFileListManager(length = 1)
        },
        {
            sendFilesExecution(
                command = SendFilesCommand().also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = passwordArguments.first(), value = "password")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                    it.setFiles(listOf("file1", "file2"))
                },
            )
        },
        {
            DesktopServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )

    @UnitTest
    fun sendFilesSkipCompressEverything() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()
            coEvery { fileNamesTooLong(any(), any()) } returns FileTransfer.COMPRESS_EVERYTHING.toName
            every { sendFilesCollecting(any(), any()) } returns mockSourceFileListManager(length = 1)
        },
        {
            sendFilesExecution(
                command = SendFilesCommand().also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = passwordArguments.first(), value = "password")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                    it.setFiles(listOf("file1", "file2"))
                },
            )
        },
        {
            DesktopServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )

    @UnitTest
    fun sendFilesSkipCompressInvalid() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()
            coEvery { fileNamesTooLong(any(), any()) } returns FileTransfer.COMPRESS_INVALID.toName
            every { sendFilesCollecting(any(), any()) } returns mockSourceFileListManager(length = 1)
        },
        {
            sendFilesExecution(
                command = SendFilesCommand().also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = passwordArguments.first(), value = "password")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                    it.setFiles(listOf("file1", "file2"))
                },
            )
        },
        {
            DesktopServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )
}
