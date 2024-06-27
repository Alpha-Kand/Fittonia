package commandHandler.executeCommand

import DesktopServer
import OutputIO.printlnIO
import SettingsManager
import commandHandler.ServerCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.server_init_cannot_start_no_dump_path
import fittonia.composeapp.generated.resources.server_init_cannot_start_no_password
import fittonia.composeapp.generated.resources.server_init_server_already_started
import fittonia.composeapp.generated.resources.server_init_server_started

suspend fun serverExecution(command: ServerCommand) {
    if (!SettingsManager.settingsManager.hasServerPassword()) {
        printlnIO(Res.string.server_init_cannot_start_no_password)
        return
    }
    if (!SettingsManager.settingsManager.hasDumpPath()) {
        printlnIO(Res.string.server_init_cannot_start_no_dump_path)
        return
    }

    if (DesktopServer.init(port = command.getPort())) {
        printlnIO(Res.string.server_init_server_started)
    } else {
        printlnIO(Res.string.server_init_server_already_started)
    }
}
