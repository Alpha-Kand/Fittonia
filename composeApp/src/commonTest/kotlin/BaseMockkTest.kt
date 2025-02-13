import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class BaseMockkTest {

    @BeforeEach
    open fun beforeEachBaseMockk() {
        MockKAnnotations.init(this)

        // Config
        mockkObject(MockConfig)
        every { MockConfig.IS_MOCKING } returns true
    }

    @AfterEach
    open fun afterEachBaseMockk() {
        clearAllMocks()
    }
}
