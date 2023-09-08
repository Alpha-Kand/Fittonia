package commandHandler.executeCommand

import commandHandler.ServerPasswordCommand
import settingsManager.SettingsManager

fun serverPasswordExecution(command: ServerPasswordCommand) {
    val settingsManager = SettingsManager.settingsManager
    if (settingsManager.settings.serverPassword == "") {
        if (command.getOldPassword() != null) {
            println("No previous password found. New password not saved.")
            return
        }
    } else {
        if (command.getOldPassword() == null) {
            println("Please provide current (old) password to confirm changes.")
            return
        }

        if (settingsManager.settings.serverPassword != command.getOldPassword()) {
            println("Old password was incorrect, please try again.")
            return
        }
    }

    settingsManager.setServerPassword(command.getNewPassword())
    println("Password saved. You will need to provide this new password if you want to change the default password again.")
}
