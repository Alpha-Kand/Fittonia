package commandHandler.executeCommand

import OutputIO.errorIO
import OutputIO.printlnIO
import OutputIO.successIO
import OutputIO.warningIO
import SettingsManager
import commandHandler.DumpCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.dump_path_current
import fittonia.composeapp.generated.resources.dump_path_doesnt_exist
import fittonia.composeapp.generated.resources.dump_path_not_empty_warning
import fittonia.composeapp.generated.resources.dump_path_not_set
import fittonia.composeapp.generated.resources.dump_path_not_valid_directory
import fittonia.composeapp.generated.resources.dump_path_not_writable
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

suspend fun dumpExecution(command: DumpCommand) {
    val settingsManager = SettingsManager.settingsManager
    command.getDumpPath()?.let { path ->
        val expandedPath = if (path.startsWith("~" + File.separator)) {
            System.getProperty("user.home") + File.separator + path.substring(2)
        } else {
            path
        }
        val dumpPath = Paths.get(expandedPath).toAbsolutePath()
        if (Files.exists(dumpPath)) {
            if (Files.isDirectory(dumpPath)) {
                if (Files.isWritable(dumpPath)) {
                    settingsManager.setDumpPath(dumpPath.toString())
                    if (Files.list(dumpPath).findFirst().isPresent) {
                        successIO()
                        warningIO(Res.string.dump_path_not_empty_warning)
                    }
                } else {
                    errorIO(Res.string.dump_path_not_writable)
                }
            } else {
                errorIO(Res.string.dump_path_not_valid_directory)
            }
        } else {
            errorIO(Res.string.dump_path_doesnt_exist)
        }
    } ?: run {
        if (settingsManager.settings.dumpPath.isEmpty()) {
            printlnIO(Res.string.dump_path_not_set)
        } else {
            printlnIO(Res.string.dump_path_current, settingsManager.settings.dumpPath)
        }
    }
}
