package commandHandler.executeCommand

import OutputIO.printlnIO
import OutputIO.successIO
import SettingsManagerDesktop
import commandHandler.SetDefaultPortCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.current_default_port_is_set_to

suspend fun setDefaultPortExecution(command: SetDefaultPortCommand) {
    val settingsManager = SettingsManagerDesktop.settingsManager
    if (command.getClear()) {
        settingsManager.clearDefaultPort()
    } else if (command.getPort() != null) {
        settingsManager.setDefaultPort(requireNotNull(command.getPort()))
        successIO()
    } else {
        printlnIO(Res.string.current_default_port_is_set_to, settingsManager.defaultPort)
    }
}
