import KotterSession.kotter
import com.varabyte.kotter.foundation.input.OnInputEnteredScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.concurrent.ConcurrentScopedData
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
        every { Config.isMockking } returns true

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
    }

    @AfterEach
    fun afterEachBaseMockk() {
        clearAllMocks()
    }
}
