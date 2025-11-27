package commandHandler.executeCommand

import OutputIO.successIO
import SettingsManagerDesktop
import commandHandler.RemoveCommand

suspend fun removeExecution(command: RemoveCommand) {
    if (SettingsManagerDesktop.settingsManager.removeDestination(name = command.getName())) {
        successIO()
    } else {
        // TODO errorIO("No destination with that name found.") - After release
    }
}
