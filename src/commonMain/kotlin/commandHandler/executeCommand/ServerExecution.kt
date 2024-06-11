package commandHandler.executeCommand

import Config.OSMapper.serverEngineJar
import KotterSession.kotter
import LocalServer
import OutputIO.printlnIO
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
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import kotterSection
import printLine
import sendApproval
import sendConfirmation
import sendDeny
import settingsManager.SettingsManager
import java.io.File
import kotlin.io.path.Path

fun serverExecution(command: ServerCommand) {
    val mainServerSocket = HMeadowSocketServer.createServerSocket(command.getPort())
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
            serverEngine.foo(lock = mainServerSocket)
        }.start()
    }
}

fun HMeadowSocketServer.foo(lock: Any) {
    var jobPath = "???"

    while (true) {
        when (val flag = receiveString()) {
            NEED_JOB_NAME,
            HAVE_JOB_NAME,
            -> getJobName(flag = flag)

            RECEIVING_ITEM -> printReceivingItem(lock = lock, jobPath = jobPath)
            SHARE_JOB_NAME -> jobPath = receiveString().split('/').last()

            DONE -> Unit
        }
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
        else -> throw Exception() // TODO
    }
    var nonConflictedJobName: String = autoJobName

    var i = 0
    while (FileOperations.exists(Path(path = settingsManager.settings.dumpPath + "/$nonConflictedJobName"))) {
        nonConflictedJobName = autoJobName + "_" + settingsManager.getAutoJobName()
        i++
        if (i > 20) {
            throw Exception() // TODO
        }
    }
    sendString(settingsManager.settings.dumpPath + "/$nonConflictedJobName")
}

private fun HMeadowSocketServer.passwordIsValid() = SettingsManager.settingsManager.checkPassword(receiveString())

fun HMeadowSocketServer.handleCommandServerEngine(serverParent: HMeadowSocketClient) {
    /*
    handleCommand(
        onSendFilesCommand = {
            // TODO it
            serverSendFilesExecution(serverParent = serverParent)
        },
        onSendMessageCommand = {
            // TODO it
            println("Received message from client.")
            println(receiveString())
            // printLine(text = "Received message from client.")
            // printLine(receiveString(), color = 0xccc949) // Lightish yellow.
        },
        onAddDestination = {
            // TODO it
            if (!it) {
                println("Client attempted to add this server as destination, password refused.") // todo
            } else {
                if (receiveBoolean()) {
                    println("Client added this server as a destination.")
                } else {
                    println("Client failed to add this server as a destination.") // todo
                }
            }
        },
        onInvalidCommand = {
            println("Received invalid server command from client.") // todo
        },
    )
     */
}

fun HMeadowSocketServer.handleCommand(
    onSendFilesCommand: (Boolean, HMeadowSocketServer) -> Unit,
    onSendMessageCommand: (Boolean, HMeadowSocketServer) -> Unit,
    onAddDestination: (Boolean, HMeadowSocketServer) -> Unit,
    onInvalidCommand: (String) -> Unit,
) {
    val receivedCommand = receiveString()
    val command: ServerCommandFlag
    try {
        command = requireNotNull(receivedCommand.toCommandFlag())
    } catch (e: Exception) {
        sendDeny()
        onInvalidCommand(receivedCommand)
        return
    }
    sendConfirmation()
    val passwordIsValid = passwordIsValid()
    sendApproval(choice = passwordIsValid)
    when (command) {
        ServerCommandFlag.SEND_FILES -> onSendFilesCommand(passwordIsValid, this)
        ServerCommandFlag.SEND_MESSAGE -> onSendMessageCommand(passwordIsValid, this)
        ServerCommandFlag.ADD_DESTINATION -> onAddDestination(passwordIsValid, this)
    }
}

fun startServerEngine(inputTokens: List<String>) = Thread {
    val currentDirectory = System.getProperty("user.dir")
    val serverEngineCmdLine = StringBuilder()
        .append("java -jar $currentDirectory/build/compose/jars/$serverEngineJar")
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

fun serverExecution2(command: ServerCommand) {
    if (SettingsManager.settingsManager.hasServerPassword()) {
        if (LocalServer.init(port = command.getPort())) {
            printlnIO("Server started. ⏳ Waiting for clients.")
        } else {
            printlnIO("Server already started.")
        }
    } else {
        printlnIO("No server password set. Set it with the `server-password` command.")
    }
}
