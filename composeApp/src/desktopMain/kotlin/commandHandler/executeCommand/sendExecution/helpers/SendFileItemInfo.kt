package commandHandler.executeCommand.sendExecution.helpers

import commandHandler.FileTransfer

/**
 * Represents a single file or directory that is intended to be send via the "Send File" command.
 *
 * @property absolutePath Absolute path for the local file system. e.g. /user/dir/file.txt
 * @property relativePath Path of item relative to the root path the user supplied. e.g. /user/dir -> /dir/file.txt
 * @property nameIsTooLong Whether or not the file's relative path is too long for the 'server' to receive.
 * @property isFile Whether or not the item is a file versus directory.
 */
internal class SendFileItemInfo(
    val absolutePath: String,
    val relativePath: String,
    val nameIsTooLong: Boolean,
    prefix: String,
) {
    val isFile: Boolean = prefix == FileTransfer.filePrefix
}
