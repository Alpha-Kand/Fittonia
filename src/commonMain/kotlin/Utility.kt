import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.rgb
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import hmeadowSocket.HMeadowSocket

fun <T> requireNull(value: T?) {
    if (value != null) {
        throw IllegalStateException("Required value was NOT null.")
    }
}

fun Session.reportHMSocketError(e: HMeadowSocket.HMeadowSocketError) = section {
    red { text("Error: ") }
    when (e.errorType) {
        HMeadowSocket.SocketErrorType.CLIENT_SETUP -> textLine("There was an error setting up CLIENT")
        HMeadowSocket.SocketErrorType.SERVER_SETUP -> textLine("There was an error setting up SERVER")
    }
    e.message?.let {
        textLine("       $it")
    } ?: textLine(".")
}.run()

fun Session.printLine(text: String, color: Color = Color.WHITE) = section { color(color); textLine(text) }.run()
fun Session.printLine(text: String, color: Int) = section { rgb(color); textLine(text) }.run()
fun Session.printLine() = section { textLine() }.run()
