package commandHandler.executeCommand

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.ServerPasswordCommand
import settingsManager.SettingsManager

fun Session.serverPasswordExecution(command: ServerPasswordCommand) = section {
    val settingsManager = SettingsManager.settingsManager
    if (settingsManager.settings.serverPassword == "") {
        if (command.getOldPassword() != null) {
            textLine(text = "No previous password found. New password not saved.")
            return@section
        }
    } else {
        if (command.getOldPassword() == null) {
            textLine(text = "Please provide current (old) password to confirm changes.")
            return@section
        }

        if (settingsManager.settings.serverPassword != command.getOldPassword()) {
            textLine(text = "Old password was incorrect, please try again.")
            return@section
        }
    }

    settingsManager.setServerPassword(command.getNewPassword())
    textLine(text = "Password saved. You will need to provide this new password if you want to change the default password again.")
}.run()
