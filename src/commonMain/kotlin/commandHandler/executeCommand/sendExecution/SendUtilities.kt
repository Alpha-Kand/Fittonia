package commandHandler.executeCommand.sendExecution

import java.io.File
import java.util.concurrent.TimeUnit

fun startClientEngine(inputTokens: List<String>) = Thread {
    val currentDirectory = System.getProperty("user.dir")
    val clientEngineCmdLine = StringBuilder()
        .append("java -jar $currentDirectory/build/compose/jars/FittoniaClientEngine-linux-x64-1.0.jar")
    inputTokens.forEach {
        clientEngineCmdLine.append(' ')
        clientEngineCmdLine.append(it)
    }

    ProcessBuilder(*clientEngineCmdLine.toString().split(' ').toTypedArray())
        .directory(File(currentDirectory))
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(60, TimeUnit.MINUTES)
}.start()
