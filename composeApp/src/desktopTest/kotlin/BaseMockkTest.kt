import KotterSession.kotter
import com.varabyte.kotter.foundation.input.OnInputEnteredScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.concurrent.ConcurrentScopedData
import fileOperations.FileOperations
import fileOperations.FittoniaTempFileBase
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class BaseMockkTest {

    @BeforeEach
    fun beforeEachBaseMockk() {
        MockKAnnotations.init(this)

        // Config
        mockkObject(Config)
        every { Config.IS_MOCKING } returns true

        // Kotter
        kotter = mockk(relaxed = true)
        mockkConstructor(RunScope::class)
        // Kotter.Section.onInputEntered
        every {
            anyConstructed<RunScope>().data.tryPut(
                key = any<ConcurrentScopedData.Key<OnInputEnteredScope.() -> Unit>>(),
                provideInitialValue = any(),
            )
        } returns true

        // FileOperations
        mockkObject(FileOperations)
        mockkObject(FileOperations.FileOperationMock)

        // SettingsManager
        mockkObject(SettingsManager.settingsManager)

        // FittoniaTempFile
        mockkObject(FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines)
    }

    @AfterEach
    fun afterEachBaseMockk() {
        clearAllMocks()
        OutputIO.flush()
    }

    fun mockkFileOperationsFileExists(exists: Boolean) {
        every { FileOperations.FileOperationMock.exists } returns exists
    }

    fun mockkFittoniaTempFileMockFileLines(fileLines: MutableList<String>? = null) {
        val list = fileLines ?: mutableListOf(
            "0\n",
            "F?ccc\n",
            "/aaa/bbb/ccc\n",
            "0\n",
            "F?fff\n",
            "/ddd/eee/fff\n",
        )
        every { FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines.fileLines } returns list
    }
}
