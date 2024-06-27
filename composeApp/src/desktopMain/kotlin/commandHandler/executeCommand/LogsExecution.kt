package commandHandler.executeCommand

import DesktopServer
import OutputIO.printlnIO
import com.varabyte.kotter.foundation.text.Color

fun logsExecution() {
    DesktopServer.instance().getLogs().forEach {
        val color = when (it.type) {
            DesktopServer.LogType.NORMAL -> Color.WHITE
            DesktopServer.LogType.WARNING -> Color.YELLOW
            DesktopServer.LogType.ERROR -> Color.RED
            DesktopServer.LogType.DEBUG -> Color.BLUE
        }
        if (it.jobId != null) {
            printlnIO("${it.timeStamp} (${it.jobId}): ${it.message}", color = color)
        } else {
            printlnIO("${it.timeStamp}: ${it.message}", color = color)
        }
    }
}
