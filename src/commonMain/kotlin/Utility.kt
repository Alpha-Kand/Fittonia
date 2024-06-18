import com.varabyte.kotter.runtime.Session
import commandHandler.ServerFlagsString
import hmeadowSocket.HMeadowSocket

fun <T> requireNull(value: T?) {
    if (value != null) {
        throw IllegalStateException("Required value was NOT null.")
    }
}

inline fun Boolean.alsoIfTrue(block: () -> Unit): Boolean {
    if (this) {
        block()
    }
    return this
}

fun HMeadowSocket.sendApproval(choice: Boolean) {
    if (choice) {
        sendConfirmation()
    } else {
        sendDeny()
    }
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
