import hmeadowSocket.HMeadowSocket

fun <T> requireNull(value: T?) {
    if (value != null) {
        throw IllegalStateException("Required value was NOT null.")
    }
}

fun reportHMSocketError(e: HMeadowSocket.HMeadowSocketError) {
    print("Error: ")
    when (e.errorType) {
        HMeadowSocket.SocketErrorType.CLIENT_SETUP -> println("There was an error setting up CLIENT")
        HMeadowSocket.SocketErrorType.SERVER_SETUP -> println("There was an error setting up SERVER")
    }
    e.message?.let {
        println("       $it")
    } ?: println(".")
}
