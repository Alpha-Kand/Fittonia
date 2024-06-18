package commandHandler.executeCommand

import BaseSocketScriptTest
import LocalServer
import UnitTest
import commandHandler.SendMessageCommand
import commandHandler.executeCommand.sendExecution.sendMessageExecution
import commandHandler.ipArguments
import commandHandler.passwordArguments
import commandHandler.portArguments
import commandHandler.setupSendCommandClient2
import io.mockk.every
import io.mockk.mockkStatic
import settingsManager.SettingsManager

private class SendMessageScriptTest : BaseSocketScriptTest() {
    @UnitTest
    fun default() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient2)
            every { setupSendCommandClient2(any()) } returns generateClient()
            every { SettingsManager.settingsManager.checkPassword(any()) } returns true
        },
        {
            sendMessageExecution(
                command = SendMessageCommand(message = "This is a message.").also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = passwordArguments.first(), value = "password")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                },
            )
        },
        {
            LocalServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )
}
