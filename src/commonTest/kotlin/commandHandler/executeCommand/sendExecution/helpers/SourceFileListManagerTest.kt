package commandHandler.executeCommand.sendExecution.helpers

import BaseMockkTest
import UnitTest
import fileOperationWrappers.FileOperations
import fileOperationWrappers.FittoniaTempFileBase.FittoniaBufferedWriterBase.BufferedWriterFileLines
import fileOperationWrappers.FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
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
        every { FileOperations.FileOperationMock.exists } returns true
        every { FileOperations.FileOperationMock.isRegularFile } returns true
        every { TempFileLines.fileLines } returns mutableListOf(
            "0\n",
            "F?ccc\n",
            "/aaa/bbb/ccc\n",
            "0\n",
            "F?fff\n",
            "/ddd/eee/fff\n",
        )
        SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc", "/ddd/eee/fff"),
            serverDestinationDirLength = 100,
            onItemFound = {},
        )
        TempFileLines.fileLines.forEachIndexed { index, line ->
            Assertions.assertEquals(line, BufferedWriterFileLines.bufferedFileLines[index])
        }
    }

    @UnitTest
    fun bufferedWriterWritesDirectoriesCorrectly() = runTest {
        every { FileOperations.FileOperationMock.exists } returns true
        every { FileOperations.FileOperationMock.isRegularFile } returns false
        every { FileOperations.FileOperationMock.paths } returns listOf(
            Path("/aaa/bbb/ccc/ggg"),
            Path("/aaa/bbb/ccc/hhh"),
        )
        every { TempFileLines.fileLines } returns mutableListOf(
            "0\n",
            "D?ccc/ggg\n",
            "/aaa/bbb/ccc/ggg\n",
            "0\n",
            "D?ccc/hhh\n",
            "/aaa/bbb/ccc/hhh\n",
        )
        SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc"),
            serverDestinationDirLength = 100,
            onItemFound = {},
        )
        TempFileLines.fileLines.forEachIndexed { index, line ->
            Assertions.assertEquals(line, BufferedWriterFileLines.bufferedFileLines[index])
        }
    }

    @UnitTest
    fun userGivenPathsDontExist() = runTest {
        every { FileOperations.FileOperationMock.exists } returns false
        SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc", "/ddd/eee/fff"),
            serverDestinationDirLength = 100,
            onItemFound = {},
        )
        Assertions.assertTrue(BufferedWriterFileLines.bufferedFileLines.isEmpty())
    }

    @UnitTest
    fun givenFilePathTooLongForServer() = runTest {
        every { FileOperations.FileOperationMock.exists } returns true
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
            Assertions.assertEquals(line, BufferedWriterFileLines.bufferedFileLines[index])
        }
    }

    @UnitTest
    fun recordsFileNamesThatAreTooLong() = runTest {
        every { FileOperations.FileOperationMock.exists } returns true
        val sourceFileListManager = SourceFileListManager(
            userInputPaths = listOf("/aaa/bbb/ccc", "ddd/eee/fff", "ggg/hhh/i"),
            serverDestinationDirLength = 126,
            onItemFound = {},
        )
        Assertions.assertEquals(listOf("ccc", "fff"), sourceFileListManager.filesNameTooLong)
        Assertions.assertTrue(sourceFileListManager.foundItemNameTooLong)
        Assertions.assertEquals(3, sourceFileListManager.totalItemCount)
        Assertions.assertEquals(1, sourceFileListManager.validItemCount)
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

        Assertions.assertEquals(2, itemInfos.size)

        itemInfos[0].run {
            Assertions.assertTrue(isFile)
            Assertions.assertEquals("/user/home/file", absolutePath)
            Assertions.assertEquals("/home/file", relativePath)
            Assertions.assertFalse(nameIsTooLong)
        }

        itemInfos[1].run {
            Assertions.assertFalse(isFile)
            Assertions.assertEquals("/user/home/directory", absolutePath)
            Assertions.assertEquals("/home/directory", relativePath)
            Assertions.assertTrue(nameIsTooLong)
        }
    }

    @UnitTest
    fun onFileFoundCallbackCalled() = runTest {
        every { FileOperations.FileOperationMock.exists } returns true
        var onFileFoundCount = 0
        SourceFileListManager(
            userInputPaths = listOf("/aaa", "/bbb", "/ccc"),
            serverDestinationDirLength = 100,
            onItemFound = {
                onFileFoundCount++
            },
        )
        Assertions.assertEquals(3, onFileFoundCount - 1)
    }
}
