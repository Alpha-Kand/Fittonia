package commandHandler.executeCommand

import LocalServer
import OutputIO.printlnIO
import com.varabyte.kotter.foundation.text.Color

fun logsExecution() {
    LocalServer.instance().getLogs().forEach {
        val color = when (it.type) {
            LocalServer.LogType.NORMAL -> Color.WHITE
            LocalServer.LogType.WARNING -> Color.YELLOW
            LocalServer.LogType.ERROR -> Color.RED
            LocalServer.LogType.DEBUG -> Color.BLUE
        }
        if (it.jobId != null) {
            printlnIO("${it.timeStamp} (${it.jobId}): ${it.message}", color = color)
        } else {
            printlnIO("${it.timeStamp}: ${it.message}", color = color)
        }
    }
}
