package commandHandler.executeCommand

import commandHandler.SendFilesCommand
import commandHandler.ServerFlags
import commandHandler.receiveConfirmation
import commandHandler.sendPassword
import hmeadowSocket.HMeadowSocketClient
import settingsManager.SettingsManager
import java.net.InetAddress

fun sendFilesExecution(command: SendFilesCommand) {
    val client = if (command.getDestination() != null) {
        val destination = SettingsManager
            .settingsManager
            .settings
            .destinations.find { it.name == command.getDestination() }
        if (destination == null) {
            println("No registered destination found with the name: " + command.getDestination())
            return
        }
        HMeadowSocketClient(
            ipAddress = InetAddress.getByName(destination.ip), // "localhost"),
            port = command.getPort(),
        )
    } else {
        HMeadowSocketClient(
            ipAddress = InetAddress.getByName(command.getIP()),
            port = command.getPort(),
        )
    }

    client.sendInt(ServerFlags.SEND_FILES)
    if (client.receiveConfirmation()) {
        if (!client.sendPassword(command.getPassword())) {
            println("Server refused password.")
            return
        }

        client.sendString(command.getFiles())
        println("TODO Send files")
    } else {
        println("Connected, but request refused.")
    }
}
