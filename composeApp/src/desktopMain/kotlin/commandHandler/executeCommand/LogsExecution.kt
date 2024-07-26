package commandHandler.executeCommand

import DesktopServer
import OutputIO.printlnIO
import com.varabyte.kotter.foundation.text.Color

fun logsExecution() {
    DesktopServer.instance().getLogs().forEach {
        val color = when (it.type) {
            LogType.NORMAL -> Color.WHITE
            LogType.WARNING -> Color.YELLOW
            LogType.ERROR -> Color.RED
            LogType.DEBUG -> Color.BLUE
        }
        if (it.jobId != null) {
            printlnIO("${it.timeStamp} (${it.jobId}): ${it.message}", color = color)
        } else {
            printlnIO("${it.timeStamp}: ${it.message}", color = color)
        }
    }
}
