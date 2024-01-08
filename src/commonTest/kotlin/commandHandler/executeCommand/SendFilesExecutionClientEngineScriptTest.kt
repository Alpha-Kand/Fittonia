package commandHandler.executeCommand

import BaseSocketScriptTest
import UnitTest
import commandHandler.FileTransfer
import commandHandler.executeCommand.sendExecution.helpers.SendFileItemInfo
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.executeCommand.sendExecution.sendFilesClientSetup
import commandHandler.executeCommand.sendExecution.sendFilesNormal
import commandHandler.executeCommand.sendExecution.sendItem
import commandHandler.executeCommand.sendExecution.sendItemCount
import fileOperationWrappers.FileOperations
import fileOperationWrappers.FittoniaTempFileBase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import settingsManager.SettingsManager
import kotlin.io.path.Path

private class SendFilesScriptTest : BaseSocketScriptTest() {

    @BeforeEach
    fun beforeEach() {
        mockkObject(SettingsManager.settingsManager)
        every { SettingsManager.settingsManager.getAutoJobName() } returns "jobAutoName"
        every { SettingsManager.settingsManager.settings.dumpPath } returns "dumpPath"

        mockkObject(FileOperations)
        mockkObject(FileOperations.FileOperationMock)
        every { FileOperations.FileOperationMock.exists } returns false
    }

    @UnitTest
    fun sendFilesSetupWithJobName() = runSocketScriptTest {
        launchSockets(
            clientBlock = {
                val serverDirLength = generateClient().sendFilesClientSetup(job = "job")
                assertEquals("dumpPath/job".length, serverDirLength)
            },
            serverBlock = {
                val jobPath = generateServer().sendFilesServerSetup()
                assertEquals("dumpPath/job", jobPath)
            },
        )
    }

    @UnitTest
    fun sendFilesSetupWithoutJobName() = runSocketScriptTest {
        launchSockets(
            clientBlock = {
                val serverDirLength = generateClient().sendFilesClientSetup(job = null)
                assertEquals("dumpPath/jobAutoName".length, serverDirLength)
            },
            serverBlock = {
                val jobPath = generateServer().sendFilesServerSetup()
                assertEquals("dumpPath/jobAutoName", jobPath)
            },
        )
    }

    @UnitTest
    fun sendItemFile() = runSocketScriptTest {
        launchSockets(
            clientBlock = {
                generateClient().sendItem(
                    sendFileItemInfo = SendFileItemInfo(
                        absolutePath = "absolutePath",
                        relativePath = "relativePath",
                        nameIsTooLong = false,
                        prefix = FileTransfer.filePrefix,
                    ),
                )
            },
            serverBlock = {
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
    }

    @UnitTest
    fun sendItemDirectory() = runSocketScriptTest {
        launchSockets(
            clientBlock = {
                generateClient().sendItem(
                    sendFileItemInfo = SendFileItemInfo(
                        absolutePath = "absolutePath",
                        relativePath = "relativePath",
                        nameIsTooLong = false,
                        prefix = FileTransfer.dirPrefix,
                    ),
                )
            },
            serverBlock = {
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
    }

    @UnitTest
    fun sendClientInfoValid() = runSocketScriptTest {
        launchSockets(
            clientBlock = {
                generateClient().sendItemCount(itemCount = 5)
            },
            serverBlock = {
                val (dir, count, cancel) = generateServer().waitForItemCount()
                assertFalse(dir.toString().isEmpty())
                assertEquals(5, count)
                assertFalse(cancel)
            },
        )
    }

    @UnitTest
    fun sendClientInfoEmpty() = runSocketScriptTest {
        launchSockets(
            clientBlock = {
                generateClient().sendItemCount(itemCount = null)
            },
            serverBlock = {
                val (dir, count, cancel) = generateServer().waitForItemCount()
                assertTrue(dir.toString().isEmpty())
                assertEquals(-1, count)
                assertTrue(cancel)
            },
        )
    }

    @UnitTest
    fun sendFilesNormal() = runSocketScriptTest {
        launchSockets(
            clientBlock = {
                mockkObject(FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines)
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
            serverBlock = {
                val server = generateServer()
                server.waitForItemCount()
                repeat(times = 2) {
                    server.receiveItem("", Path(""), { }, { })
                }
            },
        )
    }
}
