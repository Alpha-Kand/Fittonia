package fileOperationWrappers

import Config
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path

internal object FileOperations {

    object FileOperationMock {
        val exists = true
        val isRegularFile = true
        val paths = emptyList<Path>()
    }

    fun exists(path: Path): Boolean {
        return if (Config.IS_MOCKING) {
            FileOperationMock.exists
        } else {
            Files.exists(path)
        }
    }

    fun isRegularFile(path: Path): Boolean {
        return if (Config.IS_MOCKING) {
            FileOperationMock.isRegularFile
        } else {
            Files.isRegularFile(path)
        }
    }

    fun recursiveDirSearch(startPath: Path, block: (Path, isFile: Boolean) -> Unit) {
        return if (Config.IS_MOCKING) {
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

    fun createDirectory(path: Path): Path {
        return if (Config.IS_MOCKING) {
            path
        } else {
            Files.createDirectory(path)
        }
    }

    fun move(source: Path, destination: Path): Path {
        return if (Config.IS_MOCKING) {
            destination
        } else {
            Files.move(source, destination)
        }
    }

    fun createTempDirectory(prefix: String): Path {
        return if (Config.IS_MOCKING) {
            Path(prefix)
        } else {
            Files.createTempDirectory(prefix)
        }
    }
}
