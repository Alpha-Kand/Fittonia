package commandHandler.executeCommand

import BaseSocketScriptTest
import DesktopServer
import SettingsManagerDesktop
import UnitTest
import commandHandler.SendMessageCommand
import commandHandler.accessCodeArguments
import commandHandler.executeCommand.sendExecution.sendMessageExecution
import commandHandler.ipArguments
import commandHandler.portArguments
import commandHandler.setupSendCommandClient
import io.mockk.every
import io.mockk.mockkStatic

private class SendMessageScriptTest : BaseSocketScriptTest() {
    @UnitTest
    fun default() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()
            every { SettingsManagerDesktop.settingsManager.checkAccessCode(any()) } returns true
        },
        {
            sendMessageExecution(
                command = SendMessageCommand(message = "This is a message.").also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = accessCodeArguments.first(), value = "accesscode")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                },
            )
        },
        {
            DesktopServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )
}
