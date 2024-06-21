package commandHandler.executeCommand

import commandHandler.RemoveCommand
import errorIO
import settingsManager.SettingsManager
import successIO

fun removeExecution(command: RemoveCommand) {
    if (SettingsManager.settingsManager.removeDestination(name = command.getName())) {
        successIO()
    } else {
        errorIO("No destination with that name found.")
    }
}
