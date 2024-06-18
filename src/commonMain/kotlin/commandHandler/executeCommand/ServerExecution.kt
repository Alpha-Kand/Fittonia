package commandHandler.executeCommand

import LocalServer
import OutputIO.printlnIO
import commandHandler.ServerCommand
import serverInitCannotStartNoDumpPath
import serverInitCannotStartNoPassword
import serverInitServerAlreadyStarted
import serverInitServerStarted
import settingsManager.SettingsManager

fun serverExecution(command: ServerCommand) {
    if (!SettingsManager.settingsManager.hasServerPassword()) {
        printlnIO(serverInitCannotStartNoPassword)
        return
    }
    if (!SettingsManager.settingsManager.hasDumpPath()) {
        printlnIO(serverInitCannotStartNoDumpPath)
        return
    }

    if (LocalServer.init(port = command.getPort())) {
        printlnIO(serverInitServerStarted)
    } else {
        printlnIO(serverInitServerAlreadyStarted)
    }
}
