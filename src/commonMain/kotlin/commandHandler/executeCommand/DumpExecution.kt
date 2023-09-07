package commandHandler.executeCommand

import commandHandler.DumpCommand
import settingsManager.SettingsManager
import java.nio.file.Files
import java.nio.file.Paths

fun dumpExecution(command: DumpCommand) {
    val settingsManager = SettingsManager.settingsManager
    command.getDumpPath()?.let {
        val path = Paths.get(it).toAbsolutePath()
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                settingsManager.setDumpPath(path.toString())

                if (Files.list(path).findFirst().isPresent) {
                    println("Warning: New dump path is not empty.")
                }
            } else {
                println("Supplied path was not a valid directory.")
            }
        } else {
            println("Supplied path does not exist.")
        }
    } ?: run {
        if (settingsManager.settings.dumpPath.isEmpty()) {
            println("No dump path set.")
        } else {
            println("Current dump path: " + settingsManager.settings.dumpPath)
        }
    }
}
