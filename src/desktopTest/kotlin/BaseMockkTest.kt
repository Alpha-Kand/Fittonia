import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class BaseMockkTest {

    @BeforeEach
    fun beforeEach() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }
}
