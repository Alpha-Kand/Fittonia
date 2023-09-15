package commandHandler.executeCommand

import commandHandler.FileTransfer
import commandHandler.SendFilesCommand
import commandHandler.ServerFlags
import commandHandler.receiveConfirmation
import commandHandler.sendPassword
import hmeadowSocket.HMeadowSocketClient
import settingsManager.SettingsManager
import java.io.File
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors
import kotlin.io.path.isRegularFile

fun sendFilesExecution(command: SendFilesCommand) {
    val client = if (command.getDestination() != null) {
        val destination = SettingsManager
            .settingsManager
            .settings
            .destinations.find { it.name == command.getDestination() }
        if (destination == null) {
            println("No registered destination found with the name: " + command.getDestination())
            return
        }
        HMeadowSocketClient(
            ipAddress = InetAddress.getByName(destination.ip), // "localhost"),
            port = command.getPort(),
        )
    } else {
        HMeadowSocketClient(
            ipAddress = InetAddress.getByName(command.getIP()),
            port = command.getPort(),
        )
    }

    client.sendInt(ServerFlags.SEND_FILES)
    if (client.receiveConfirmation()) {
        if (!client.sendPassword(command.getPassword())) {
            println("Server refused password.")
            return
        }
        command.getJob()?.let { jobName ->
            client.sendInt(ServerFlags.HAVE_JOB_NAME)
            client.sendString(jobName)
        } ?: client.sendInt(ServerFlags.NEED_JOB_NAME)

        val tempSourceListFile = File.createTempFile(FileTransfer.tempPrefix, FileTransfer.tempSuffix)
        val fileCount = writeSourceFileList(sourceList = command.getFiles(), tempSourceListFile = tempSourceListFile)
        client.sendInt(fileCount)

        var sendFlag: SendFileFlag = SendFileFlag.RELATIVE_PATH
        tempSourceListFile.bufferedReader().lines().collect(Collectors.toList()).forEach { path ->
            when (sendFlag) {
                SendFileFlag.RELATIVE_PATH -> { // Relative path.
                    print("Sending: ${path.substring(startIndex = 2)}")
                    client.sendString(path)
                    sendFlag = if (path.substring(0, 2) == FileTransfer.filePrefix) {
                        SendFileFlag.FILE_PATH
                    } else {
                        SendFileFlag.FOLDER_PATH
                    }
                }

                SendFileFlag.FILE_PATH -> {
                    client.sendFile(filePath = path.toString())
                    sendFlag = SendFileFlag.RELATIVE_PATH
                    println(" Done.")
                }

                SendFileFlag.FOLDER_PATH -> {
                    sendFlag = SendFileFlag.RELATIVE_PATH
                    println(" Done.")
                }
            }
        }
    } else {
        println("Connected, but request refused.")
    }
}

private enum class SendFileFlag {
    RELATIVE_PATH,
    FILE_PATH,
    FOLDER_PATH,
}

private fun writeSourceFileList(sourceList: List<String>, tempSourceListFile: File): Int {
    val fileList = getFileList(input = sourceList)
    val sourceListFileWriter = tempSourceListFile.bufferedWriter()
    fileList.forEach { pathPair ->
        val (absolutePath, relativePath) = pathPair
        sourceListFileWriter.write((if (absolutePath.isRegularFile()) FileTransfer.filePrefix else FileTransfer.dirPrefix) + relativePath)
        sourceListFileWriter.newLine()
        sourceListFileWriter.write(absolutePath.toString())
        sourceListFileWriter.newLine()
    }
    sourceListFileWriter.close()
    return fileList.size
}

private fun getFileList(input: List<String>): List<Pair<Path, Path>> {
    val fileList = mutableListOf<Pair<Path, Path>>()
    input.forEach { inputPath ->
        val path = Paths.get(inputPath)
        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                // println(path.fileName)
                fileList.add(path to path.fileName)
            } else {
                Files.find(
                    path,
                    Int.MAX_VALUE,
                    { filePath: Path?, fileAttr: BasicFileAttributes -> fileAttr.isRegularFile || fileAttr.isDirectory },
                ).forEach { x: Path? ->
                    x?.let {
                        // println(path.parent.relativize(it))
                        fileList.add(x to path.parent.relativize(it))
                    }
                }
            }
        } else {
            println("\"$inputPath\" doesn't exist.")
        }
    }

    return fileList.toList()
}
