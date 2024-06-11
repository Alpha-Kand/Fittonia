package commandHandler.executeCommand

import KotterRunType
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
import commandHandler.canContinueSendCommand2
import commandHandler.executeCommand.sendExecution.sendCommandExecution
import hmeadowSocket.HMeadowSocketClient
import kotterSection
import settingsManager.SettingsManager
import java.net.InetAddress

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

fun addExecution2(command: AddCommand) {
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
    val client = HMeadowSocketClient(
        ipAddress = InetAddress.getByName(command.getIP()),
        port = command.getPort(),
        timeoutMillis = 2000L,
    )
    if (canContinueSendCommand2(command = command, client = client)) {
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
