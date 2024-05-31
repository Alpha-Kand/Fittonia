package commandHandler.executeCommand

import BaseSocketScriptTest
import UnitTest
import commandHandler.SendFilesCommand
import commandHandler.ServerFlagsString
import commandHandler.executeCommand.sendExecution.foundFileNamesTooLong
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.executeCommand.sendExecution.sendCommandExecution
import commandHandler.executeCommand.sendExecution.sendFilesCollecting
import hmeadowSocket.HMeadowSocketServer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import reportTextLine

private class SendFilesClientToParentScriptTest : BaseSocketScriptTest() {

    @UnitTest
    fun reportTextLine() = runSocketScriptTest2(
        setupBlock = {},
        {
            val parent = generateClient()
            parent.reportTextLine(text = "text line")
            parent.sendString(ServerFlagsString.DONE)
        },
        {
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServerAnyPort(any(), any(), any()) } returns generateServer()
            sendCommandExecution(command = mockk(relaxed = true), inputTokens = emptyList())
        },
    )

    @UnitTest
    fun fileNamesTooLong() = runSocketScriptTest2(
        setupBlock = {},
        {
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
        {
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServerAnyPort(any(), any(), any()) } returns generateServer()
            sendCommandExecution(command = mockk(relaxed = true), inputTokens = emptyList())
        },
    )

    @UnitTest
    fun sendFilesCollecting() = runSocketScriptTest2(
        setupBlock = {},
        {
            val parent = generateClient()
            parent.sendFilesCollecting(
                command = mockk<SendFilesCommand> {
                    every { getFiles() } returns listOf("/aaa/bbb/ccc", "/ddd/eee/fff")
                },
                serverDestinationDirLength = 126,
            )
            parent.sendString(ServerFlagsString.DONE)
        },
        {
            mockkObject(HMeadowSocketServer)
            every { HMeadowSocketServer.createServerAnyPort(any(), any(), any()) } returns generateServer()
            sendCommandExecution(command = mockk(relaxed = true), inputTokens = emptyList())
        },
    )
}