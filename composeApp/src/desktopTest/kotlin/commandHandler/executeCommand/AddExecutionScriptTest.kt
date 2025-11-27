package commandHandler.executeCommand

import BaseSocketScriptTest
import DesktopServer
import SettingsManagerDesktop
import UnitTest
import commandHandler.AddCommand
import commandHandler.accessCodeArguments
import commandHandler.ipArguments
import commandHandler.nameArguments
import commandHandler.portArguments
import commandHandler.setupSendCommandClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkStatic

private class AddExecutionScriptTest : BaseSocketScriptTest() {
    @UnitTest
    fun default() = runSocketScriptTest2(
        setupBlock = {
            mockkStatic(::setupSendCommandClient)
            every { setupSendCommandClient(any()) } returns generateClient()
            every { SettingsManagerDesktop.settingsManager.settings.destinations } returns emptyList()
            coEvery { SettingsManagerDesktop.settingsManager.addDestination(any(), any(), any()) } returns Unit
            every { SettingsManagerDesktop.settingsManager.checkAccessCode(any()) } returns true
        },
        {
            addExecution(
                command = AddCommand().also {
                    it.addArg(argumentName = ipArguments.first(), value = "ip code")
                    it.addArg(argumentName = accessCodeArguments.first(), value = "accesscode")
                    it.addArg(argumentName = portArguments.first(), value = "1234")
                    it.addArg(argumentName = nameArguments.first(), value = "name")
                },
            )
        },
        {
            DesktopServer.instance().handleCommand(generateServer(), jobId = 100)
        },
    )
}
