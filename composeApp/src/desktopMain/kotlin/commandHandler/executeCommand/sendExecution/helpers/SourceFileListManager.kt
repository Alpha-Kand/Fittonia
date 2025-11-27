package commandHandler.executeCommand.sendExecution.helpers

import MockConfig
import commandHandler.FileTransfer
import commandHandler.FileTransfer.Companion.getPrefix
import commandHandler.FileTransfer.Companion.stripPrefix
import fileOperations.FileOperations
import fileOperations.FittoniaTempFileBase
import java.nio.file.Path
import java.nio.file.Paths

/**
 * This class searches the user supplied paths to find all files and directories to transfer. Each file/directory is
 * referred to as an 'item'.
 * Afterwards, you can iterate over the records of each discovered file.
 *
 * @param userInputPaths List of user supplied paths pointing to either files or directories. e.g. "/user/home/foo"
 * @param serverDestinationDirLength The maximum amount of characters a transfered file/directory's name can be based on
 * the space the connected server can receive.
 * @param onItemFound Callback called on every file/directory added to the records.
 *
 * @property filesNameTooLong List of files whose names are too long for the connected server to receive.
 * @property foundItemNameTooLong Convenience property equivalent to 'filesNameTooLong.isNotEmpty()'.
 */
internal class SourceFileListManager(
    private val userInputPaths: List<String>,
    private val serverDestinationDirLength: Int,
    private val onItemFound: (Int) -> Unit,
) {
    companion object {
        private const val MAX_PATH_CHAR_LENGTH = 127
    }

    private val tempSourceListFile: FittoniaTempFileBase = if (MockConfig.IS_MOCKING) {
        FittoniaTempFileBase.FittoniaTempFileMock
    } else {
        FittoniaTempFileBase.FittoniaTempFile()
    }

    val filesNameTooLong: List<String>
    val foundItemNameTooLong: Boolean
    val totalItemCount: Int
    val validItemCount: Int

    init {
        val fileList = getFileList()
        filesNameTooLong = fileList.writeFileList()
        foundItemNameTooLong = filesNameTooLong.isNotEmpty()
        totalItemCount = fileList.size
        validItemCount = totalItemCount - filesNameTooLong.size
    }

    /**
     * Iterates over every discovered item from all the supplied paths.
     */
    fun forEachItem(block: (SendFileItemInfo) -> Unit) {
        val fileListCrawler = mutableListOf<String>()
        tempSourceListFile.lineStream { line ->
            fileListCrawler.add(line)
            if (fileListCrawler.size == 3) {
                block(
                    SendFileItemInfo(
                        nameIsTooLong = fileListCrawler[0].first() == '1',
                        relativePath = fileListCrawler[1].stripPrefix(),
                        absolutePath = fileListCrawler[2],
                        prefix = fileListCrawler[1].getPrefix(),
                    ),
                )
                fileListCrawler.clear()
            }
        }
    }

    private data class FilePaths(
        val absolutePath: Path,
        val relativePath: Path,
        val isFile: Boolean,
    )

    private fun List<FilePaths>.writeFileList(): List<String> {
        val fileNameTooLong = mutableListOf<String>()
        tempSourceListFile.bufferedWrite { bufferedWriter ->
            this.forEach { pathPair ->
                val absolutePath = pathPair.absolutePath.toString()
                val relativePath = pathPair.relativePath.toString()
                if (relativePath.isPathTooLong()) {
                    fileNameTooLong.add(relativePath)
                }
                bufferedWriter.writeFileLengthStatus(relativePath = relativePath)
                bufferedWriter.newLine()
                bufferedWriter.writePrefixedPath(pathPair = pathPair)
                bufferedWriter.newLine()
                bufferedWriter.write(absolutePath)
                bufferedWriter.newLine()
            }
        }
        return fileNameTooLong
    }

    private fun String.isPathTooLong() = serverDestinationDirLength + length > MAX_PATH_CHAR_LENGTH

    private fun FittoniaTempFileBase.FittoniaBufferedWriterBase.writeFileLengthStatus(relativePath: String) {
        val lengthStatus = if (relativePath.isPathTooLong()) {
            "1"
        } else {
            "0"
        }
        this.write(lengthStatus)
    }

    private fun FittoniaTempFileBase.FittoniaBufferedWriterBase.writePrefixedPath(pathPair: FilePaths) {
        val prefix = if (pathPair.isFile) {
            FileTransfer.filePrefix
        } else {
            FileTransfer.dirPrefix
        }
        this.write(prefix + pathPair.relativePath)
    }

    private fun getFileList(): List<FilePaths> {
        val fileList = mutableListOf<FilePaths>()
        val doesntExist = mutableListOf<String>()
        userInputPaths.forEach { inputPathString ->
            val path = Paths.get(inputPathString)
            if (FileOperations.exists(path)) {
                if (FileOperations.isRegularFile(path)) {
                    fileList.addRegularFile(path = path)
                } else {
                    fileList.addDirectoryAndContents(path = path)
                }
            } else {
                doesntExist.add(inputPathString) // TODO - After release
            }
        }
        onItemFound(fileList.size)
        return fileList
    }

    private fun MutableList<FilePaths>.addRegularFile(path: Path) {
        this.add(
            FilePaths(
                absolutePath = path,
                relativePath = path.fileName,
                isFile = true,
            ),
        )
        onItemFound(this.size)
    }

    private fun MutableList<FilePaths>.addDirectoryAndContents(path: Path) {
        FileOperations.recursiveDirSearch(startPath = path) { p: Path, isFile: Boolean ->
            this.add(
                FilePaths(
                    absolutePath = p,
                    relativePath = path.parent.relativize(p),
                    isFile = isFile,
                ),
            )
            onItemFound(this.size)
        }
    }
}
