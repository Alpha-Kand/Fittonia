import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.rgb
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.ServerFlags
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient

fun <T> requireNull(value: T?) {
    if (value != null) {
        throw IllegalStateException("Required value was NOT null.")
    }
}

fun Session.reportHMSocketError(e: HMeadowSocket.HMeadowSocketError) = section {
    red { text("Error: ") }
    when (e.errorType) {
        HMeadowSocket.SocketErrorType.CLIENT_SETUP -> textLine(text = "There was an error setting up CLIENT")
        HMeadowSocket.SocketErrorType.SERVER_SETUP -> textLine(text = "There was an error setting up SERVER")
        HMeadowSocket.SocketErrorType.COULD_NOT_BIND_SERVER_TO_GIVEN_PORT ->
            textLine(text = "Could not create server on given port.")

        HMeadowSocket.SocketErrorType.COULD_NOT_FIND_AVAILABLE_PORT ->
            textLine(text = "Could not find any available ports.")
    }
    e.message?.let {
        textLine(text = "       $it")
    } ?: textLine(text = ".")
}.run()

fun Session.printLine(text: String, color: Color = Color.WHITE) = section { color(color); textLine(text) }.run()
fun Session.printLine(text: String, color: Int) = section { rgb(color); textLine(text) }.run()
fun Session.printLine() = section { textLine() }.run()

fun HMeadowSocketClient.reportTextLine(text: String, color: Color = Color.WHITE) {
    sendInt(message = ServerFlags.PRINT_LINE)
    sendInt(message = color.ordinal)
    sendString(message = text)
}
