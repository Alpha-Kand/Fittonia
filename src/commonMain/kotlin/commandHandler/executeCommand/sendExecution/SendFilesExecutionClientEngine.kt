package commandHandler.executeCommand.sendExecution

import commandHandler.FileTransfer
import commandHandler.SendFilesCommand
import commandHandler.ServerFlags
import commandHandler.canContinue
import commandHandler.setupSendCommandClient
import hmeadowSocket.HMeadowSocketClient
import reportTextLine
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors
import kotlin.io.path.isRegularFile

fun sendFilesExecutionClientEngine(command: SendFilesCommand, parent: HMeadowSocketClient) {
    val client = setupSendCommandClient(command = command)
    client.sendInt(ServerFlags.SEND_FILES)
    if (canContinue(command = command, client = client, parent = parent)) {
        command.getJob()?.let { jobName ->
            client.sendInt(ServerFlags.HAVE_JOB_NAME)
            client.sendString(jobName)
        } ?: client.sendInt(ServerFlags.NEED_JOB_NAME)

        val serverDestinationDirLength = client.receiveInt()
        val tempSourceListFile = File.createTempFile(FileTransfer.tempPrefix, FileTransfer.tempSuffix)
        val (fileCount, fileNameTooLong) = writeSourceFileList(
            sourceList = command.getFiles(),
            tempSourceListFile = tempSourceListFile,
            serverDestinationDirLength = serverDestinationDirLength,
            parent = parent,
        )

        val fileListCrawler = mutableListOf<String>()
        var skipped = 0
        if (fileNameTooLong) {
            parent.sendInt(ServerFlags.FILE_NAMES_TOO_LONG)
            parent.sendInt(serverDestinationDirLength)
            tempSourceListFile.bufferedReader().lines().collect(Collectors.toList()).forEach {
                fileListCrawler.add(it)

                if (fileListCrawler.size == 3) {
                    val fileLengthStatus = fileListCrawler[0]
                    val relativePath = fileListCrawler[1]

                    if (fileLengthStatus.first() == '1') {
                        parent.sendInt(ServerFlags.HAS_MORE)
                        parent.sendString(relativePath.substring(startIndex = FileTransfer.prefixLength))
                        skipped += 1
                    }
                    fileListCrawler.clear()
                }
            }
            parent.sendInt(ServerFlags.DONE)
        }

        var skipInvalid = false

        when (parent.receiveInt()) {
            FileTransfer.NORMAL -> {
                client.sendInt(ServerFlags.CONFIRM)
                client.sendInt(fileCount)
            }

            FileTransfer.CANCEL -> {
                client.sendInt(ServerFlags.CANCEL_SEND_FILES)
                parent.sendInt(ServerFlags.DONE)
                return
            }

            FileTransfer.SKIP_INVALID -> {
                val sendingFileAmount = fileCount - skipped
                parent.reportTextLine(text = "Sending $sendingFileAmount files.")
                client.sendInt(ServerFlags.CONFIRM)
                client.sendInt(fileCount - skipped)
                skipInvalid = true
            }

            FileTransfer.COMPRESS_EVERYTHING -> {
                println("COMPRESS FILES")
            }

            FileTransfer.COMPRESS_INVALID -> {
                println("COMPRESS FILES")
            }

            else -> {
                println("send files else")
            }
        }

        tempSourceListFile.bufferedReader().lines().collect(Collectors.toList()).forEach {
            fileListCrawler.add(it)

            if (fileListCrawler.size == 3) {
                val fileLengthStatus = fileListCrawler[0]
                val relativePath = fileListCrawler[1]
                val absolutePath = fileListCrawler[2]

                if (fileLengthStatus.first() == '1') {
                    if (!skipInvalid) {
                        // todo: compress or throw error I think.
                    }
                } else {
                    client.sendString(relativePath)
                    if (relativePath.substring(0, FileTransfer.prefixLength) == FileTransfer.filePrefix) {
                        client.sendFile(filePath = absolutePath)
                    }
                }
                fileListCrawler.clear()
            }
        }
    }
}

private fun writeSourceFileList(
    sourceList: List<String>,
    tempSourceListFile: File,
    serverDestinationDirLength: Int,
    parent: HMeadowSocketClient,
): Pair<Int, Boolean> {
    var fileNameTooLong = false
    val fileList = getFileList(input = sourceList, parent = parent)
    val sourceListFileWriter = tempSourceListFile.bufferedWriter()
    fileList.forEach { pathPair ->
        val (absolutePath, relativePath) = pathPair
        fileNameTooLong = fileNameTooLong || serverDestinationDirLength + relativePath.toString().length > 127
        sourceListFileWriter.write(if (serverDestinationDirLength + relativePath.toString().length > 127) "1" else "0")
        sourceListFileWriter.newLine()
        sourceListFileWriter.write((if (absolutePath.isRegularFile()) FileTransfer.filePrefix else FileTransfer.dirPrefix) + relativePath)
        sourceListFileWriter.newLine()
        sourceListFileWriter.write(absolutePath.toString())
        sourceListFileWriter.newLine()
    }
    sourceListFileWriter.close()

    return fileList.size to fileNameTooLong
}

private fun getFileList(
    input: List<String>,
    parent: HMeadowSocketClient,
): List<Pair<Path, Path>> {
    parent.reportTextLine(text = "Finding files to send...\uD83D\uDD0E")
    parent.sendInt(message = ServerFlags.SEND_FILES_COLLECTING)

    val fileList = mutableListOf<Pair<Path, Path>>()
    val doesntExist = mutableListOf<String>()
    input.forEach { inputPath ->
        val path = Paths.get(inputPath)
        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                fileList.add(path to path.fileName)
                parent.reportFindingFiles(amount = fileList.size)
            } else {
                Files.find(
                    path,
                    Int.MAX_VALUE,
                    { _: Path?, fileAttr: BasicFileAttributes -> fileAttr.isRegularFile || fileAttr.isDirectory },
                ).forEach { x: Path? ->
                    x?.let {
                        fileList.add(x to path.parent.relativize(it))
                        parent.reportFindingFiles(amount = fileList.size)
                    }
                }
            }
        } else {
            doesntExist.add(inputPath) // TODO
        }
    }
    parent.reportFindingFiles(amount = fileList.size)
    parent.sendInt(message = ServerFlags.DONE)
    return fileList.toList()
}

private fun HMeadowSocketClient.reportFindingFiles(amount: Int) {
    sendInt(message = ServerFlags.HAS_MORE)
    sendInt(message = amount)
}
