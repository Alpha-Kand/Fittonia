package commandHandler.executeCommand

import LocalServer
import OutputIO.printlnIO
import com.varabyte.kotter.foundation.text.Color

fun logsExecution() {
    LocalServer.instance().getLogs().forEach {
        when (it.type) {
            LocalServer.LogType.NORMAL -> printlnIO("${it.timeStamp}: ${it.message}")
            LocalServer.LogType.WARNING -> printlnIO("${it.timeStamp}: ${it.message}", color = Color.YELLOW)
            LocalServer.LogType.ERROR -> printlnIO("${it.timeStamp}: ${it.message}", color = Color.RED)
        }
    }
}
