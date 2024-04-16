package commandHandler.executeCommand.sendExecution

import Config.OSMapper.clientEngineJar
import java.io.File
import java.util.concurrent.TimeUnit

fun startClientEngine(inputTokens: List<String>) = Thread {
    val currentDirectory = System.getProperty("user.dir")
    val clientEngineCmdLine = StringBuilder()
        .append("java -jar $currentDirectory/build/compose/jars/$clientEngineJar")
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
