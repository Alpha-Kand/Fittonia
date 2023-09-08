package commandHandler.executeCommand

import commandHandler.RemoveCommand
import settingsManager.SettingsManager

fun removeExecution(command: RemoveCommand) {
    SettingsManager.settingsManager.removeDestination(name = command.getName())
}
