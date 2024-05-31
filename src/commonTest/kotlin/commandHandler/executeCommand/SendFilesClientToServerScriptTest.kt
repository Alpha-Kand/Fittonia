package commandHandler.executeCommand

import BaseSocketScriptTest
import UnitTest
import commandHandler.FileTransfer
import commandHandler.executeCommand.sendExecution.helpers.FileZipper
import commandHandler.executeCommand.sendExecution.helpers.SendFileItemInfo
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.executeCommand.sendExecution.sendFilesClientSetup
import commandHandler.executeCommand.sendExecution.sendFilesCompressEverything
import commandHandler.executeCommand.sendExecution.sendFilesCompressInvalid
import commandHandler.executeCommand.sendExecution.sendFilesNormal
import commandHandler.executeCommand.sendExecution.sendFilesSkipInvalid
import commandHandler.executeCommand.sendExecution.sendItem
import commandHandler.executeCommand.sendExecution.sendItemCount
import fileOperationWrappers.FileOperations
import fileOperationWrappers.FittoniaTempFileBase
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import settingsManager.SettingsManager
import kotlin.io.path.Path

private class SendFilesClientToServerScriptTest : BaseSocketScriptTest() {

    @BeforeEach
    fun beforeEach() {
        every { SettingsManager.settingsManager.getAutoJobName() } returns "jobAutoName"
        every { SettingsManager.settingsManager.settings.dumpPath } returns "dumpPath"

        every { FileOperations.FileOperationMock.exists } returns false

        mockkConstructor(FileZipper::class)
        every { anyConstructed<FileZipper>().zipItem(any()) } just Runs
        every { anyConstructed<FileZipper>().finalize(any()) } answers {
            this.firstArg<(String) -> Unit>().invoke("")
        }
    }

    @UnitTest
    fun sendFilesSetupWithJobName() = runSocketScriptTest2(
        setupBlock = {},
        {
            val serverDirLength = generateClient().sendFilesClientSetup(job = "job")
            assertEquals("dumpPath/job".length, serverDirLength)
        },
        {
            val jobPath = generateServer().sendFilesServerSetup(serverParent = generateClient(key = "parent"))
            assertEquals("dumpPath/job", jobPath)
        },
        {
            val parent = generateServer("parent")
            parent.receiveString()
            parent.receiveString()
            parent.sendString("dumpPath/job")
        },
    )

    @UnitTest
    fun sendFilesSetupWithoutJobName() = runSocketScriptTest2(
        setupBlock = {},
        {
            val serverDirLength = generateClient().sendFilesClientSetup(job = null)
            assertEquals("dumpPath/jobAutoName".length, serverDirLength)
        },
        {
            val jobPath = generateServer().sendFilesServerSetup(serverParent = generateClient(key = "parent"))
            assertEquals("dumpPath/jobAutoName", jobPath)
        },
        {
            val parent = generateServer("parent")
            parent.receiveString()
            parent.sendString("dumpPath/jobAutoName")
        },
    )

    @UnitTest
    fun sendItemFile() = runSocketScriptTest2(
        setupBlock = {},
        {
            generateClient().sendItem(
                sendFileItemInfo = SendFileItemInfo(
                    absolutePath = "absolutePath",
                    relativePath = "relativePath",
                    nameIsTooLong = false,
                    prefix = FileTransfer.filePrefix,
                ),
            )
        },
        {
            val onGetRelativePath = mockk<(String) -> Unit>(relaxed = true)
            val onDone = mockk<() -> Unit>(relaxed = true)
            generateServer().receiveItem(
                jobPath = "jobPath",
                tempReceivingFolder = Path("tempReceivingFolder"),
                onGetRelativePath = onGetRelativePath,
                onDone = onDone,
            )

            verify { onGetRelativePath(any()) }
            verify { onDone() }
            verify { FileOperations.move(any(), any()) }
            verify(exactly = 0) { FileOperations.createDirectory(any()) }
        },
    )

    @UnitTest
    fun sendItemDirectory() = runSocketScriptTest2(
        setupBlock = {},
        {
            generateClient().sendItem(
                sendFileItemInfo = SendFileItemInfo(
                    absolutePath = "absolutePath",
                    relativePath = "relativePath",
                    nameIsTooLong = false,
                    prefix = FileTransfer.dirPrefix,
                ),
            )
        },
        {
            val onGetRelativePath = mockk<(String) -> Unit>(relaxed = true)
            val onDone = mockk<() -> Unit>(relaxed = true)
            generateServer().receiveItem(
                jobPath = "jobPath",
                tempReceivingFolder = Path("tempReceivingFolder"),
                onGetRelativePath = onGetRelativePath,
                onDone = onDone,
            )

            verify { onGetRelativePath(any()) }
            verify { onDone() }
            verify(exactly = 0) { FileOperations.move(any(), any()) }
            verify { FileOperations.createDirectory(any()) }
        },
    )

    @UnitTest
    fun sendClientInfoValid() = runSocketScriptTest2(
        setupBlock = {},
        {
            val client = generateClient()
            client.sendItemCount(itemCount = 5)
        },
        {
            val (dir, count, cancel) = generateServer().waitForItemCount()
            assertFalse(dir.toString().isEmpty())
            assertEquals(5, count)
            assertFalse(cancel)
        },
    )

    @UnitTest
    fun sendClientInfoEmpty() = runSocketScriptTest2(
        setupBlock = {},
        {
            generateClient().sendItemCount(itemCount = null)
            println("clientBlock done")
        },
        {
            val (dir, count, cancel) = generateServer().waitForItemCount()
            assertTrue(dir.toString().isEmpty())
            assertEquals(-1, count)
            assertTrue(cancel)
            println("serverBlock done")
        },
    )

    @UnitTest
    fun sendFilesNormal() = runSocketScriptTest2(
        setupBlock = {},
        {
            every { FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines.fileLines } returns mutableListOf(
                "0\n",
                "F?ccc\n",
                "/aaa/bbb/ccc\n",
                "0\n",
                "F?fff\n",
                "/ddd/eee/fff\n",
            )
            generateClient().sendFilesNormal(
                sourceFileListManager = SourceFileListManager(
                    userInputPaths = emptyList(),
                    serverDestinationDirLength = 100,
                    onItemFound = {},
                ),
            )
        },
        {
            val server = generateServer()
            server.waitForItemCount()
            repeat(times = 2) {
                server.receiveItemAndReport(
                    jobPath = "job path",
                    tempReceivingFolder = mockk(relaxed = true),
                    serverParent = mockk(relaxed = true),
                )
            }
        },
    )

    @UnitTest
    fun sendFilesSkipInvalid() = runSocketScriptTest2(
        setupBlock = {},
        {
            every { FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines.fileLines } returns mutableListOf(
                "0\n",
                "F?ccc\n",
                "/aaa/bbb/ccc\n",
                "1\n",
                "F?fff\n",
                "/ddd/eee/fff\n",
                "0\n",
                "F?ggg\n",
                "/hhh/iii/jjj\n",
            )
            generateClient().sendFilesSkipInvalid(
                sourceFileListManager = SourceFileListManager(
                    userInputPaths = emptyList(),
                    serverDestinationDirLength = 100,
                    onItemFound = {},
                ),
            )
        },
        {
            val server = generateServer()
            server.waitForItemCount()
            repeat(times = 2) {
                server.receiveItemAndReport(
                    jobPath = "job path",
                    tempReceivingFolder = mockk(relaxed = true),
                    serverParent = mockk(relaxed = true),
                )
            }
        },
    )

    @UnitTest
    fun sendFilesCompressEverything() = runSocketScriptTest2(
        setupBlock = {},
        {
            every { FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines.fileLines } returns mutableListOf(
                "0\n",
                "F?ccc\n",
                "/aaa/bbb/ccc\n",
                "0\n",
                "F?fff\n",
                "/ddd/eee/fff\n",
            )
            generateClient().sendFilesCompressEverything(
                sourceFileListManager = SourceFileListManager(
                    userInputPaths = emptyList(),
                    serverDestinationDirLength = 100,
                    onItemFound = {},
                ),
            )
            verify(exactly = 2) { anyConstructed<FileZipper>().zipItem(any()) }
            verify(exactly = 1) { anyConstructed<FileZipper>().finalize(any()) }
        },
        {
            val server = generateServer()
            val (_, count, _) = server.waitForItemCount()
            server.receiveItemAndReport(
                jobPath = "job path",
                tempReceivingFolder = mockk(relaxed = true),
                serverParent = mockk(relaxed = true),
            )
            assertEquals(1, count)
        },
    )

    @UnitTest
    fun sendFilesCompressInvalid() = runSocketScriptTest2(
        setupBlock = {},
        {
            every { FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines.fileLines } returns mutableListOf(
                "0\n",
                "F?ccc\n",
                "/aaa/bbb/ccc\n",
                "1\n",
                "F?fff\n",
                "/ddd/eee/fff\n",
                "0\n",
                "F?ggg\n",
                "/hhh/iii/jjj\n",
                "1\n",
                "F?kkk\n",
                "/lll/mmm/nnn\n",
            )
            generateClient().sendFilesCompressInvalid(
                sourceFileListManager = SourceFileListManager(
                    userInputPaths = emptyList(),
                    serverDestinationDirLength = 100,
                    onItemFound = {},
                ),
            )
        },
        {
            val server = generateServer()
            server.waitForItemCount()
            repeat(times = 3) {
                server.receiveItemAndReport(
                    jobPath = "job path",
                    tempReceivingFolder = mockk(relaxed = true),
                    serverParent = mockk(relaxed = true),
                )
            }
        },
    )
}
