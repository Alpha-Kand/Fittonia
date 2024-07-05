package commandHandler.executeCommand

import OutputIO.printlnIO
import SettingsManagerDesktop
import commandHandler.ServerPasswordCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.confirm_password_update
import fittonia.composeapp.generated.resources.confirm_password_update_failed
import fittonia.composeapp.generated.resources.no_previous_password_found
import fittonia.composeapp.generated.resources.password_saved

suspend fun serverPasswordExecution(command: ServerPasswordCommand) {
    val settingsManager = SettingsManagerDesktop.settingsManager
    if (settingsManager.settings.serverPassword.isNullOrEmpty()) {
        if (command.getOldPassword() != null) {
            printlnIO(Res.string.no_previous_password_found)
            return
        }
    } else {
        if (command.getOldPassword() == null) {
            printlnIO(Res.string.confirm_password_update)
            return
        }

        if (settingsManager.settings.serverPassword != command.getOldPassword()) {
            printlnIO(Res.string.confirm_password_update_failed)
            return
        }
    }
    settingsManager.setServerPassword(command.getNewPassword())
    printlnIO(Res.string.password_saved)
}
