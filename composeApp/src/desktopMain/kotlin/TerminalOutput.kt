import KotterSession.kotter
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.green
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.yellow
import com.varabyte.kotter.runtime.MainRenderScope
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

private fun kotterSection(block: MainRenderScope.() -> Unit) = kotter.section(block).run()
fun printLine(text: String, color: Color = Color.WHITE) = kotterSection {
    color(color)
    textLine(text)
}

data object OutputIO {
    private val outputIO = mutableListOf<String>()

    fun printlnIO(text: String, color: Color = Color.WHITE) {
        if (Config.IS_MOCKING) {
            outputIO.add(text)
        } else {
            printLine(text, color)
        }
    }

    fun printlnIO() {
        if (Config.IS_MOCKING) {
            outputIO.add("\n")
        } else {
            printLine("\n")
        }
    }

    suspend fun printlnIO(resource: StringResource, color: Color = Color.WHITE, vararg params: Any) {
        val text = getString(resource, *params)
        if (Config.IS_MOCKING) {
            outputIO.add(text)
        } else {
            printLine(text, color)
        }
    }

    suspend fun printlnIO(resource: StringResource, vararg params: Any) {
        val text = getString(resource, *params)
        if (Config.IS_MOCKING) {
            outputIO.add(text)
        } else {
            printLine(text, Color.WHITE)
        }
    }

    suspend fun warningIO(resource: StringResource) {
        val text = getString(resource)
        if (Config.IS_MOCKING) {
            outputIO.add(text)
        } else {
            kotterSection {
                yellow { text("Warning: ") }
                textLine(text)
            }
        }
    }

    suspend fun errorIO(resource: StringResource) {
        val text = getString(resource)
        if (Config.IS_MOCKING) {
            outputIO.add(text)
        } else {
            kotterSection {
                red { text("Error: ") }
                textLine(text)
            }
        }
    }

    suspend fun successIO(resource: StringResource? = null) {
        val text = resource?.let { getString(resource) } ?: ""
        if (Config.IS_MOCKING) {
            outputIO.add(text)
        } else {
            kotterSection {
                if (text.isEmpty()) {
                    green { textLine("Success") }
                } else {
                    green { text("Success: ") }
                    textLine(text)
                }
            }
        }
    }

    fun flush(): List<String> = outputIO.toList().also { outputIO.clear() }
}
