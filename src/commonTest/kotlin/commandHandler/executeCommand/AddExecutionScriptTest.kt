package commandHandler.executeCommand

import BaseSocketScriptTest
import LocalServer
import UnitTest
import commandHandler.AddCommand
import commandHandler.ipArguments
import commandHandler.nameArguments
import commandHandler.passwordArguments
import commandHandler.portArguments
import commandHandler.setupSendCommandClient2
import io.mockk.every
import io.mockk.mockkStatic
import settingsManager.SettingsManager

private class AddExecutionScriptTest : BaseSocketScriptTest() {
    @UnitTest
    fun default() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient2)
            every { setupSendCommandClient2(any()) } returns generateClient()
            every { SettingsManager.settingsManager.settings.destinations } returns emptyList()
            every { SettingsManager.settingsManager.addDestination(any(), any(), any()) } returns Unit
            every { SettingsManager.settingsManager.checkPassword(any()) } returns true
        },
        {
            addExecution(
                command = AddCommand().also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = passwordArguments.first(), value = "password")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                    it.addArg(argumentName = nameArguments.first(), value = "name")
                },
            )
        },
        {
            LocalServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )
}
