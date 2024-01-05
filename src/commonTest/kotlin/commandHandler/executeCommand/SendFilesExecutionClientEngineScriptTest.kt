package commandHandler.executeCommand

import BaseSocketScriptTest
import UnitTest
import commandHandler.executeCommand.sendExecution.sendFilesClientSetup
import fileOperationWrappers.FileOperations
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import settingsManager.SettingsManager

private class SendFilesScriptTest : BaseSocketScriptTest() {

    @BeforeEach
    fun beforeEach() {
        mockkObject(SettingsManager.settingsManager)
        every { SettingsManager.settingsManager.getAutoJobName() } returns "jobAutoName"
        every { SettingsManager.settingsManager.settings.dumpPath } returns "dumpPath"
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
}
