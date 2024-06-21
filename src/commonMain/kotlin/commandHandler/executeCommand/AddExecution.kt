package commandHandler.executeCommand

import KotterSession.kotter
import OutputIO.printlnIO
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.text.green
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import commandHandler.AddCommand
import commandHandler.canContinueSendCommand
import commandHandler.setupSendCommandClient2
import settingsManager.SettingsManager

fun addExecution(command: AddCommand) {
    val settingsManager = SettingsManager.settingsManager
    if (settingsManager.settings.destinations.find { it.ip == command.getIP() } != null) {
        var userInput = ""
        kotter.section {
            textLine(text = "A destination with this IP address is already registered.")
            text(text = "Are you sure you want to add another destination with this IP (")
            green { text("y") }
            text("/")
            red { text("n") }
            textLine(")?")
            input()
        }.runUntilInputEntered {
            onInputEntered { userInput = input }
        }
        if (userInput.lowercase() != "y") return
    }
    val client = setupSendCommandClient2(command = command)
    if (command.canContinueSendCommand(client = client)) {
        SettingsManager.settingsManager.addDestination(
            name = command.getName(),
            ip = command.getIP(),
            password = command.getPassword(),
        )
        printlnIO("Destination added.")
        client.sendBoolean(true)
    } else {
        printlnIO("Connected, but request refused.")
        client.sendBoolean(false)
    }
}
