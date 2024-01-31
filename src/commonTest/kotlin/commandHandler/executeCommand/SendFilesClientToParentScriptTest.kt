package commandHandler.executeCommand

import BaseSocketScriptTest
import UnitTest
import commandHandler.SendFilesCommand
import commandHandler.ServerFlagsString
import commandHandler.executeCommand.sendExecution.foundFileNamesTooLong
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.executeCommand.sendExecution.sendFilesCollecting
import commandHandler.executeCommand.sendExecution.sendFilesExecution
import hmeadowSocket.HMeadowSocketServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import reportTextLine

private class SendFilesClientToParentScriptTest : BaseSocketScriptTest() {

    @UnitTest
    fun reportTextLine() = runSocketScriptTest(
        clientBlock = {
            val parent = generateClient()
            parent.reportTextLine(text = "text line")
            parent.sendString(ServerFlagsString.DONE)
        },
        serverBlock = {
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServerAnyPort(any(), any(), any()) } returns generateServer()
            sendFilesExecution(inputTokens = emptyList())
        },
    )

    @UnitTest
    fun fileNamesTooLong() = runSocketScriptTest(
        clientBlock = {
            val sourceFileListManager = SourceFileListManager(
                userInputPaths = listOf("/aaa/bbb/ccc", "/ddd/eee/fff"),
                serverDestinationDirLength = 126,
                onItemFound = {},
            )

            val parent = generateClient()
            parent.foundFileNamesTooLong(
                sourceFileListManager = sourceFileListManager,
                serverDestinationDirLength = 126,
            )
            parent.sendString(ServerFlagsString.DONE)
        },
        serverBlock = {
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServerAnyPort(any(), any(), any()) } returns generateServer()
            sendFilesExecution(inputTokens = emptyList())
        },
    )

    @UnitTest
    fun sendFilesCollecting() = runSocketScriptTest(
        clientBlock = {
            val parent = generateClient()
            parent.sendFilesCollecting(
                command = mockk<SendFilesCommand> {
                    every { getFiles() } returns listOf("/aaa/bbb/ccc", "/ddd/eee/fff")
                },
                serverDestinationDirLength = 126,
            )
            parent.sendString(ServerFlagsString.DONE)
        },
        serverBlock = {
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServerAnyPort(any(), any(), any()) } returns generateServer()
            sendFilesExecution(inputTokens = emptyList())
        },
    )
}