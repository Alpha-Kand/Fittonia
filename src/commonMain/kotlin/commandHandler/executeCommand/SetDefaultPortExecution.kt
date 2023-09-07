package commandHandler.executeCommand

import commandHandler.SetDefaultPortCommand
import settingsManager.SettingsManager

fun setDefaultPortExecution(command: SetDefaultPortCommand) {
    val settingsManager = SettingsManager.settingsManager
    if (command.getClear()) {
        settingsManager.clearDefaultPort()
    } else if (command.getPort() != null) {
        settingsManager.setDefaultPort(requireNotNull(command.getPort()))
    } else {
        println("Current default port set to: ${settingsManager.defaultPort}")
    }
}