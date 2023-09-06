package commandHandler.executeCommand

import commandHandler.AddCommand
import commandHandler.ServerFlags
import hmeadowSocket.HMeadowSocketClient
import settingsManager.SettingsManager
import java.net.InetAddress

fun addExecution(command: AddCommand) {
    val settingsManager = SettingsManager.settingsManager
    val client = HMeadowSocketClient(
        ipAddress = InetAddress.getByName(command.getIP()),
        port = command.getPort() ?: settingsManager.defaultPort,
    )

    client.sendInt(ServerFlags.ADD_DESTINATION)
    if (client.receiveInt() == ServerFlags.CONFIRM) {
        settingsManager.addDestination(
            name = command.getName(),
            ip = command.getIP(),
            password = command.getPassword(),
        )
        println("Destination added.")
    } else {
        println("Connected, but could not add destination, request refused.")
    }
}
