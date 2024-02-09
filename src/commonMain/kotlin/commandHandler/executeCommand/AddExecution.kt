package commandHandler.executeCommand

import KotterRunType
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.text.textLine
import commandHandler.AddCommand
import commandHandler.executeCommand.sendExecution.sendCommandExecution
import kotterSection
import settingsManager.SettingsManager

fun addExecution(command: AddCommand, inputTokens: List<String>) {
    val settingsManager = SettingsManager.settingsManager
    if (settingsManager.settings.destinations.find { it.ip == command.getIP() } != null) {
        var userInput = ""
        kotterSection(
            renderBlock = {
                textLine(text = "A destination with this IP address is already registered.")
                textLine(text = "Are you sure you want to add another destination with this IP (y/n)?")
                input()
            },
            runBlock = {
                onInputEntered { userInput = input }
            },
            runType = KotterRunType.RUN_UNTIL_INPUT_ENTERED
        )
        if (userInput.lowercase() != "y") return
    }
    sendCommandExecution(command = command, inputTokens = inputTokens)
}
