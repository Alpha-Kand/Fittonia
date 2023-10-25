package commandHandler.executeCommand

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.SetDefaultPortCommand
import settingsManager.SettingsManager

fun Session.setDefaultPortExecution(command: SetDefaultPortCommand) = section {
    val settingsManager = SettingsManager.settingsManager
    if (command.getClear()) {
        settingsManager.clearDefaultPort()
    } else if (command.getPort() != null) {
        settingsManager.setDefaultPort(requireNotNull(command.getPort()))
    } else {
        textLine("Current default port set to: ${settingsManager.defaultPort}")
    }
}.run()
