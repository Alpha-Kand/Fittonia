import KotterSession.kotter
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.green
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.yellow
import com.varabyte.kotter.runtime.MainRenderScope

private fun kotterSection(block: MainRenderScope.() -> Unit) = kotter.section(block).run()
fun printLine(text: String, color: Color = Color.WHITE) = kotterSection {
    color(color)
    textLine(text)
}

fun printLine() = kotterSection { textLine() }

fun errorIO(text: String) = kotterSection {
    red { text("Error: ") }
    textLine(text)
}

fun warningIO(text: String) = kotterSection {
    yellow { text("Warning: ") }
    textLine(text)
}

fun successIO(text: String = "") = kotterSection {
    if (text.isEmpty()) {
        green { textLine("Success") }
    } else {
        green { text("Success: ") }
        textLine(text)
    }
}
