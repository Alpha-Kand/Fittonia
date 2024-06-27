package commandHandler.executeCommand.sendExecution.helpers

import commandHandler.FileTransfer
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal class FileZipper {

    private val zipFile = File.createTempFile(FileTransfer.tempPrefix, FileTransfer.tempSuffix)
    private val zipStream = ZipOutputStream(BufferedOutputStream(zipFile.outputStream()))

    fun zipItem(sendFileItemInfo: SendFileItemInfo) {
        zipStream.putNextEntry(ZipEntry(sendFileItemInfo.relativePath))
        if (sendFileItemInfo.isFile) {
            BufferedInputStream(FileInputStream(sendFileItemInfo.absolutePath)).copyTo(
                out = zipStream,
                bufferSize = 1024,
            )
        }
    }

    fun finalize(block: (String) -> Unit) {
        zipStream.close()
        block(zipFile.absolutePath)
        Files.delete(zipFile.toPath())
    }
}
