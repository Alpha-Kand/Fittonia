package commandHandler.executeCommand

import DesktopServer
import OutputIO.printlnIO
import SettingsManagerDesktop
import commandHandler.ServerCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.server_init_cannot_start_no_access_code
import fittonia.composeapp.generated.resources.server_init_cannot_start_no_dump_path
import fittonia.composeapp.generated.resources.server_init_server_already_started
import fittonia.composeapp.generated.resources.server_init_server_started

suspend fun serverExecution(command: ServerCommand) {
    if (!SettingsManagerDesktop.settingsManager.hasServerAccessCode()) {
        printlnIO(Res.string.server_init_cannot_start_no_access_code)
        return
    }
    if (!SettingsManagerDesktop.settingsManager.hasDumpPath()) {
        printlnIO(Res.string.server_init_cannot_start_no_dump_path)
        return
    }

    if (DesktopServer.init(port = command.getPort())) {
        printlnIO(Res.string.server_init_server_started)
    } else {
        printlnIO(Res.string.server_init_server_already_started)
    }
}
