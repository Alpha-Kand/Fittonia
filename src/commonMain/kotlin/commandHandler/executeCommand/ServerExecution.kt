package commandHandler.executeCommand

import Config
import KotterSession.kotter
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.green
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import commandHandler.ServerCommand
import commandHandler.ServerCommandFlag
import commandHandler.ServerCommandFlag.Companion.toCommandFlag
import commandHandler.ServerFlagsString.Companion.DONE
import commandHandler.ServerFlagsString.Companion.HAVE_JOB_NAME
import commandHandler.ServerFlagsString.Companion.NEED_JOB_NAME
import commandHandler.ServerFlagsString.Companion.RECEIVING_ITEM
import commandHandler.ServerFlagsString.Companion.SHARE_JOB_NAME
import commandHandler.serverEnginePortArguments
import fileOperationWrappers.FileOperations
import hmeadowSocket.HMeadowSocketServer
import kotterSection
import printLine
import sendApproval
import sendConfirmation
import sendDeny
import settingsManager.SettingsManager
import java.io.File
import java.net.ServerSocket
import kotlin.io.path.Path

fun serverExecution(command: ServerCommand) {
    val mainServerSocket = ServerSocket(command.getPort())
    printLine(text = "Server started.")
    while (true) {
        printLine(text = "⏳ Waiting for a client.")

        // Connect with client
        val server = HMeadowSocketServer.createServerFromSocket(serverSocket = mainServerSocket)

        Thread {
            // Create connection to server engine.
            val serverEngine = HMeadowSocketServer.createServerAnyPort(startingPort = 10778) { port ->
                // Create server engine.
                startServerEngine(listOf("${serverEnginePortArguments.first()}=$port"))
            }
            server.sendInt(serverEngine.receiveInt())
            var jobPath = "???"

            while (true) {
                when (val flag = serverEngine.receiveString()) {
                    NEED_JOB_NAME,
                    HAVE_JOB_NAME -> serverEngine.getJobName(flag = flag)

                    RECEIVING_ITEM -> serverEngine.printReceivingItem(lock = mainServerSocket, jobPath = jobPath)
                    SHARE_JOB_NAME -> {
                        jobPath = serverEngine.receiveString().split('/').last()
                    }

                    DONE -> {}
                }
            }
        }.start()
        if (Config.isMockking) return
    }
}

private fun HMeadowSocketServer.printReceivingItem(lock: Any, jobPath: String) {
    synchronized(lock) {
        var relativePath by kotter.liveVarOf(value = "")
        var complete by kotter.liveVarOf(value = false)
        kotterSection(
            renderBlock = {
                text("$jobPath: ")
                if (relativePath.isBlank()) {
                    text("Receiving data from connection...")
                } else {
                    text("Receiving: $relativePath")
                }
                if (complete) green { textLine(text = " Done.") }
            },
            runBlock = {
                relativePath = receiveString()
                receiveContinue()
                complete = true
            },
        )
    }
}

internal fun HMeadowSocketServer.getJobName(flag: String) {
    val settingsManager = SettingsManager.settingsManager
    val autoJobName = when (flag) {
        NEED_JOB_NAME -> settingsManager.getAutoJobName()
        HAVE_JOB_NAME -> receiveString()
        else -> throw Exception() //TODO
    }
    var nonConflictedJobName: String = autoJobName

    var i = 0
    while (FileOperations.exists(Path(path = settingsManager.settings.dumpPath + "/$nonConflictedJobName"))) {
        nonConflictedJobName = autoJobName + "_" + settingsManager.getAutoJobName()
        i++
        if (i > 20) {
            throw Exception() //TODO
        }
    }
    sendString(settingsManager.settings.dumpPath + "/$nonConflictedJobName")
}

private fun HMeadowSocketServer.passwordIsValid() = SettingsManager.settingsManager.checkPassword(receiveString())

fun HMeadowSocketServer.handleCommand(
    onSendFilesCommand: (Boolean) -> Unit,
    onSendMessageCommand: (Boolean) -> Unit,
    onAddDestination: (Boolean) -> Unit,
    onInvalidCommand: () -> Unit,
) {
    val command: ServerCommandFlag
    try {
        command = requireNotNull(receiveString().toCommandFlag())
    } catch (e: Exception) {
        sendDeny()
        onInvalidCommand()
        return
    }
    sendConfirmation()
    val passwordIsValid = passwordIsValid()
    sendApproval(choice = passwordIsValid)
    when (command) {
        ServerCommandFlag.SEND_FILES -> onSendFilesCommand(passwordIsValid)
        ServerCommandFlag.SEND_MESSAGE -> onSendMessageCommand(passwordIsValid)
        ServerCommandFlag.ADD_DESTINATION -> onAddDestination(passwordIsValid)
    }
}

fun startServerEngine(inputTokens: List<String>) = Thread {
    val currentDirectory = System.getProperty("user.dir")
    val serverEngineCmdLine = StringBuilder()
        .append("java -jar $currentDirectory/build/compose/jars/FittoniaServerEngine-linux-x64-1.0.jar")
    inputTokens.forEach {
        serverEngineCmdLine.append(' ')
        serverEngineCmdLine.append(it)
    }

    ProcessBuilder(*serverEngineCmdLine.toString().split(' ').toTypedArray())
        .directory(File(currentDirectory))
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
}.start()