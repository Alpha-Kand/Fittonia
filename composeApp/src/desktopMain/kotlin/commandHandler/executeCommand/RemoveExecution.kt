package commandHandler.executeCommand

import OutputIO.successIO
import SettingsManager
import commandHandler.RemoveCommand

suspend fun removeExecution(command: RemoveCommand) {
    if (SettingsManager.settingsManager.removeDestination(name = command.getName())) {
        successIO()
    } else {
        // TODO errorIO("No destination with that name found.")
    }
}
