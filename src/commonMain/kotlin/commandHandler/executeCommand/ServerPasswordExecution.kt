package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.ServerPasswordCommand
import confirmPasswordUpdate
import confirmPasswordUpdateFailed
import noPreviousPasswordFound
import passwordSaved
import settingsManager.SettingsManager

fun serverPasswordExecution(command: ServerPasswordCommand) {
    val settingsManager = SettingsManager.settingsManager
    if (settingsManager.settings.serverPassword.isNullOrEmpty()) {
        if (command.getOldPassword() != null) {
            printlnIO(noPreviousPasswordFound)
            return
        }
    } else {
        if (command.getOldPassword() == null) {
            printlnIO(confirmPasswordUpdate)
            return
        }

        if (settingsManager.settings.serverPassword != command.getOldPassword()) {
            printlnIO(confirmPasswordUpdateFailed)
            return
        }
    }
    settingsManager.setServerPassword(command.getNewPassword())
    printlnIO(passwordSaved)
}
