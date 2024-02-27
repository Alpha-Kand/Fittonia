import commandHandler.AddCommand
import commandHandler.SendFilesCommand
import commandHandler.executeCommand.sendExecution.addExecutionClientEngine
import commandHandler.executeCommand.sendExecution.sendFilesExecutionClientEngine
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketInterface
import java.net.InetAddress
import java.net.Socket

fun main(args: Array<String>) {
    val repeatTimes = args[1].toInt()
    when (args[0]) {
        "add" -> addCluster(repeatTimes = repeatTimes)
        "sendFiles" -> sendFilesCluster(repeatTimes = repeatTimes)
    }
}

private fun sendFilesCluster(repeatTimes: Int) {
    val command = SendFilesCommand(
        files = listOf(
            "/home/hunterneo/Pictures/hunter-slack-profile-kitkat.png.kra",
            "/home/hunterneo/Downloads/asdf",
        ),
        job = getRandomAlphabetString(),
    ).apply {
        this.addArg("--ip", "localhost")
        this.addArg("--password", "password")
        this.addArg("--port", "5558")
    }

    repeat(repeatTimes) {
        Thread {
            println("Thread $it starting")
            sendFilesExecutionClientEngine(
                command = command,
                parent = makeParent(it),
            )
            println("Thread $it done")
        }.start()
    }
}

private fun addCluster(repeatTimes: Int) {
    val command = AddCommand().apply {
        this.addArg("--name", getRandomAlphabetString())
        this.addArg("--ip", "localhost")
        this.addArg("--password", "password")
        this.addArg("--port", "5558")
    }

    repeat(repeatTimes) {
        Thread {
            println("Thread $it starting")
            addExecutionClientEngine(
                command = command,
                parent = makeParent(it),
            )
            println("Thread $it done")
        }.start()
    }
}

fun makeParent(count: Int) = HMeadowSocketClient(
    ipAddress = InetAddress.getByName("localhost"),
    port = 6000 + count,
    socketInterface = HMeadowSocketInterfaceDebugNothing(),
)

internal fun getRandomAlphabetString(length: Int = 10): String {
    val alphabetRange = ('A'..'Z') + ('a'..'z')
    return (1..length).map { alphabetRange.random() }.joinToString(separator = "")
}

class HMeadowSocketInterfaceDebugNothing : HMeadowSocketInterface {
    override fun bindToSocket(block: () -> Socket): Socket {
        return Socket()
    }

    override fun close() {}

    override fun sendInt(message: Int) {}

    override fun receiveInt(): Int {
        return 0
    }

    override fun sendLong(message: Long) {}

    override fun receiveLong(): Long {
        return 0L
    }

    override fun sendBoolean(message: Boolean) {}

    override fun receiveBoolean(): Boolean {
        return true
    }

    override fun sendFile(filePath: String, rename: String) {}

    override fun receiveFile(destination: String, prefix: String, suffix: String): Pair<String, String> {
        return "" to ""
    }

    override fun sendString(message: String) {}

    override fun receiveString(): String {
        return ""
    }

    override fun sendContinue() {}

    override fun receiveContinue() {}
}