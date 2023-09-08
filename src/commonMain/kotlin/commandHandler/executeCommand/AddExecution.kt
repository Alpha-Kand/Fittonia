package commandHandler.executeCommand

import commandHandler.AddCommand
import commandHandler.ServerFlags
import commandHandler.receiveConfirmation
import commandHandler.sendPassword
import hmeadowSocket.HMeadowSocketClient
import settingsManager.SettingsManager
import java.net.InetAddress

fun addExecution(command: AddCommand) {
    val settingsManager = SettingsManager.settingsManager

    if (settingsManager.settings.destinations.find { it.ip == command.getIP() } != null) {
        println("A destination with this IP address is already registered. Are you sure you want to add another destination with this IP (y/n)?")
        if (readlnOrNull()?.lowercase() != "y") return
    }

    val client = HMeadowSocketClient(
        ipAddress = InetAddress.getByName(command.getIP()),
        port = command.getPort() ?: settingsManager.defaultPort,
    )

    client.sendInt(ServerFlags.ADD_DESTINATION)
    if (client.receiveConfirmation()) {
        if (!client.sendPassword(command.getPassword())) {
            println("Server refused password.")
            return
        }
        settingsManager.addDestination(
            name = command.getName(),
            ip = command.getIP(),
            password = command.getPassword(),
        )
        println("Destination added.")
    } else {
        println("Connected, but request refused.")
    }
}
