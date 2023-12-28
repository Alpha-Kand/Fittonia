package fileOperationWrappers

import Config
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

internal object FileOperations {

    object FileOperationMock {
        val exists = true
        val isRegularFile = true
        val paths = emptyList<Path>()
    }

    fun exists(path: Path): Boolean {
        return if (Config.isMockking) {
            FileOperationMock.exists
        } else {
            Files.exists(path)
        }
    }

    fun isRegularFile(path: Path): Boolean {
        return if (Config.isMockking) {
            FileOperationMock.isRegularFile
        } else {
            Files.isRegularFile(path)
        }
    }

    fun recursiveDirSearch(startPath: Path, block: (Path, isFile: Boolean) -> Unit) {
        return if (Config.isMockking) {
            FileOperationMock.paths.forEach { path ->
                block(path, FileOperationMock.isRegularFile)
            }
        } else {
            Files.find(
                startPath,
                Int.MAX_VALUE, // Max depth.
                { _, fileAttr: BasicFileAttributes -> fileAttr.isRegularFile || fileAttr.isDirectory }, // Filter.
            ).forEach { p: Path? ->
                if (p != null) {
                    block(p, isRegularFile(p))
                }
            }
        }
    }
}
