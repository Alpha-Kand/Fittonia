package commandHandler.executeCommand.sendExecution.helpers

import BaseMockkTest
import UnitTest
import fileOperations.FileOperations
import fileOperations.FittoniaTempFileBase.FittoniaBufferedWriterBase.BufferedWriterFileLines
import fileOperations.FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import kotlin.io.path.Path

private class SourceFileListManagerTest : BaseMockkTest() {

    @BeforeEach
    fun beforeEach() {
        mockkObject(FileOperations.FileOperationMock)
        mockkObject(TempFileLines)
        BufferedWriterFileLines.bufferedFileLines.clear()
    }

    @UnitTest
    fun bufferedWriterWritesFilesCorrectly() = runTest {
        mockkFileOperationsFileExists(exists = true)
        every { FileOperations.FileOperationMock.isRegularFile } returns true
        mockkFittoniaTempFileMockFileLines()
        SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc", "/ddd/eee/fff"),
            serverDestinationDirLength = 100,
            onItemFound = {},
        )
        TempFileLines.fileLines.forEachIndexed { index, line ->
            assertEquals(line, BufferedWriterFileLines.bufferedFileLines[index])
        }
    }

    @UnitTest
    fun bufferedWriterWritesDirectoriesCorrectly() = runTest {
        mockkFileOperationsFileExists(exists = true)
        every { FileOperations.FileOperationMock.isRegularFile } returns false
        every { FileOperations.FileOperationMock.paths } returns listOf(
            Path("/aaa/bbb/ccc/ggg"),
            Path("/aaa/bbb/ccc/hhh"),
        )
        mockkFittoniaTempFileMockFileLines(
            fileLines = mutableListOf(
                "0\n",
                "D?ccc/ggg\n",
                "/aaa/bbb/ccc/ggg\n",
                "0\n",
                "D?ccc/hhh\n",
                "/aaa/bbb/ccc/hhh\n",
            ),
        )
        SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc"),
            serverDestinationDirLength = 100,
            onItemFound = {},
        )
        TempFileLines.fileLines.forEachIndexed { index, line ->
            assertEquals(line, BufferedWriterFileLines.bufferedFileLines[index])
        }
    }

    @UnitTest
    fun userGivenPathsDontExist() = runTest {
        mockkFileOperationsFileExists(exists = false)
        SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc", "/ddd/eee/fff"),
            serverDestinationDirLength = 100,
            onItemFound = {},
        )
        assertTrue(BufferedWriterFileLines.bufferedFileLines.isEmpty())
    }

    @UnitTest
    fun givenFilePathTooLongForServer() = runTest {
        mockkFileOperationsFileExists(exists = true)
        every { TempFileLines.fileLines } returns mutableListOf(
            "1\n",
            "F?ccc\n",
            "/aaa/bbb/ccc\n",
        )
        SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc"),
            serverDestinationDirLength = 126,
            onItemFound = {},
        )
        TempFileLines.fileLines.forEachIndexed { index, line ->
            assertEquals(line, BufferedWriterFileLines.bufferedFileLines[index])
        }
    }

    @UnitTest
    fun recordsFileNamesThatAreTooLong() = runTest {
        mockkFileOperationsFileExists(exists = true)
        val sourceFileListManager = SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc", "ddd/eee/fff", "ggg/hhh/i"),
            serverDestinationDirLength = 126,
            onItemFound = {},
        )
        assertEquals(listOf("ccc", "fff"), sourceFileListManager.filesNameTooLong)
        assertTrue(sourceFileListManager.foundItemNameTooLong)
        assertEquals(3, sourceFileListManager.totalItemCount)
        assertEquals(1, sourceFileListManager.validItemCount)
    }

    @UnitTest
    fun convertsFileLinesToItemInfo() = runTest {
        every {
            TempFileLines.fileLines
        } returns mutableListOf(
            "0",
            "F?/home/file",
            "/user/home/file",
            "1",
            "D?/home/directory",
            "/user/home/directory",
        )
        val sourceFileListManager = SourceFileListManager(
            userInputPaths = listOf("paths"),
            serverDestinationDirLength = 100,
            onItemFound = {},
        )

        val itemInfos = mutableListOf<SendFileItemInfo>()
        sourceFileListManager.forEachItem {
            itemInfos.add(it)
        }

        assertEquals(2, itemInfos.size)

        itemInfos[0].run {
            assertTrue(isFile)
            assertEquals("/user/home/file", absolutePath)
            assertEquals("/home/file", relativePath)
            assertFalse(nameIsTooLong)
        }

        itemInfos[1].run {
            assertFalse(isFile)
            assertEquals("/user/home/directory", absolutePath)
            assertEquals("/home/directory", relativePath)
            assertTrue(nameIsTooLong)
        }
    }

    @UnitTest
    fun onFileFoundCallbackCalled() = runTest {
        mockkFileOperationsFileExists(exists = true)
        var onFileFoundCount = 0
        SourceFileListManager(
            userInputPaths = listOf("/aaa", "/bbb", "/ccc"),
            serverDestinationDirLength = 100,
            onItemFound = {
                onFileFoundCount++
            },
        )
        assertEquals(3, onFileFoundCount - 1)
    }
}
