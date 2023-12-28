import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class BaseMockkTest {

    @BeforeEach
    fun beforeEachBaseMockk() {
        MockKAnnotations.init(this)
        mockkObject(Config)
        every { Config.isMockking } returns true
    }

    @AfterEach
    fun afterEachBaseMockk() {
        clearAllMocks()
    }
}
