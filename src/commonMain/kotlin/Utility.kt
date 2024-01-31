import KotterSession.kotter
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.rgb
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
import commandHandler.ServerFlags
import commandHandler.ServerFlagsString
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

fun <T> requireNull(value: T?) {
    if (value != null) {
        throw IllegalStateException("Required value was NOT null.")
    }
}

fun Session.reportHMSocketError(e: HMeadowSocket.HMeadowSocketError) = section {
    red { text("Error: ") }
    e.hmMessage?.let {
        textLine(text = it)
    }
    e.message?.let {
        textLine(text = "       $it")
    } ?: textLine(text = ".")
}.run()

fun printLine(text: String, color: Color = Color.WHITE) = kotter.section { color(color); textLine(text) }.run()
fun printLine(text: String, color: Int) = kotter.section { rgb(color); textLine(text) }.run()
fun printLine() = kotter.section { textLine() }.run()

fun HMeadowSocketClient.reportTextLine(text: String, color: Color = Color.WHITE) {
    sendInt(message = ServerFlags.PRINT_LINE)
    sendInt(message = color.ordinal)
    sendString(message = text)
    receiveInt()
}

fun <T> HMeadowSocket.receiveApproval(onConfirm: () -> T, onDeny: () -> T): T {
    receiveString()
    return when (receiveBoolean()) {
        true -> onConfirm()
        false -> onDeny()
    }
}

// Should be private?
fun HMeadowSocket.sendConfirmation() {
    sendString(ServerFlagsString.CONFIRM)
    sendBoolean(true)
}

fun HMeadowSocket.sendDeny() {
    sendString(ServerFlagsString.DENY)
    sendBoolean(false)
}

object KotterSession {
    lateinit var kotter: Session
}

enum class KotterRunType {
    RUN,
    RUN_UNTIL_INPUT_ENTERED,
}

@OptIn(DelicateCoroutinesApi::class)
fun kotterSection(
    renderBlock: MainRenderScope.() -> Unit,
    runBlock: RunScope.() -> Unit = {},
    runType: KotterRunType = KotterRunType.RUN,
) {
    if (Config.isMockking) {
        runBlock(RunScope(kotter.section {}, GlobalScope))
    } else {
        when (runType) {
            KotterRunType.RUN -> kotter.section(renderBlock).run(runBlock)
            KotterRunType.RUN_UNTIL_INPUT_ENTERED -> kotter.section(renderBlock).runUntilInputEntered(runBlock)
        }
    }
}
