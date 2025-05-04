package commandHandler.executeCommand

import OutputIO.printlnIO
import SettingsManagerDesktop
import commandHandler.ServerAccessCodeCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.access_code_saved
import fittonia.composeapp.generated.resources.confirm_access_code_update
import fittonia.composeapp.generated.resources.confirm_access_code_update_failed
import fittonia.composeapp.generated.resources.no_previous_access_code_found

suspend fun serverAccessCodeExecution(command: ServerAccessCodeCommand) {
    val settingsManager = SettingsManagerDesktop.settingsManager
    if (settingsManager.settings.serverAccessCode.isNullOrEmpty()) {
        if (command.getOldAccessCode() != null) {
            printlnIO(Res.string.no_previous_access_code_found)
            return
        }
    } else {
        if (command.getOldAccessCode() == null) {
            printlnIO(Res.string.confirm_access_code_update)
            return
        }

        if (settingsManager.settings.serverAccessCode != command.getOldAccessCode()) {
            printlnIO(Res.string.confirm_access_code_update_failed)
            return
        }
    }
    settingsManager.setServerAccessCode(command.getNewAccessCode())
    printlnIO(Res.string.access_code_saved)
}
