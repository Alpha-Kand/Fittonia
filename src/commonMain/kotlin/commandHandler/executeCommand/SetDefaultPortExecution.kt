package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.SetDefaultPortCommand
import currentDefaultPortIsSetTo
import settingsManager.SettingsManager
import successIO

fun setDefaultPortExecution(command: SetDefaultPortCommand) {
    val settingsManager = SettingsManager.settingsManager
    if (command.getClear()) {
        settingsManager.clearDefaultPort()
    } else if (command.getPort() != null) {
        settingsManager.setDefaultPort(requireNotNull(command.getPort()))
        successIO()
    } else {
        printlnIO(output = currentDefaultPortIsSetTo.format(settingsManager.defaultPort))
    }
}
