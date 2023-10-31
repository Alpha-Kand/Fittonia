package commandHandler.executeCommand

import FittoniaError
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.AddCommand
import commandHandler.ServerFlags
import commandHandler.receiveConfirmation
import commandHandler.sendPassword
import hmeadowSocket.HMeadowSocketClient
import printLine
import settingsManager.SettingsManager
import java.net.InetAddress

fun Session.addExecution(command: AddCommand) {
    val settingsManager = SettingsManager.settingsManager

    if (settingsManager.settings.destinations.find { it.ip == command.getIP() } != null) {
        var userInput = ""
        section {
            textLine(text = "A destination with this IP address is already registered.")
            textLine(text = "Are you sure you want to add another destination with this IP (y/n)?")
            input()
        }.runUntilInputEntered {
            onInputEntered { userInput = input }
        }
        if (userInput.lowercase() != "y") return
    }
    val client = HMeadowSocketClient(
        ipAddress = InetAddress.getByName(command.getIP()),
        port = command.getPort() ?: settingsManager.defaultPort,
    )

    client.sendInt(ServerFlags.ADD_DESTINATION)
    if (client.receiveConfirmation()) {
        if (!client.sendPassword(command.getPassword())) {
            printLine(text = "Server refused password.")
            return
        }
        try {
            settingsManager.addDestination(
                name = command.getName(),
                ip = command.getIP(),
                password = command.getPassword(),
            )
        } catch (e: FittoniaError) {
            client.sendBoolean(false)
            throw e
        }
        client.sendBoolean(true)
        printLine(text = "Destination added.")
    } else {
        printLine(text = "Connected, but request refused.")
    }
}
