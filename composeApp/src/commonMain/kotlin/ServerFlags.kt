class ServerFlagsString {
    companion object {
        const val CONFIRM = "CONFIRM"
        const val DENY = "DENY"

        const val SHARE_JOB_NAME = "SHARE_JOB_NAME"
        const val RECEIVING_ITEM = "RECEIVING_ITEM"
        const val HAVE_JOB_NAME = "HAVE_JOB_NAME"
        const val NEED_JOB_NAME = "NEED_JOB_NAME"
        const val DONE = "DONE"

        const val HAS_MORE = "HAS_MORE"
        const val PRINT_LINE = "PRINT_LINE"
        const val FILE_NAMES_TOO_LONG = "FILE_NAMES_TOO_LONG"
        const val SEND_FILES_COLLECTING = "SEND_FILES_COLLECTING"
        const val ADD_DESTINATION = "ADD_DESTINATION"
    }
}

enum class ServerCommandFlag(val text: String) {
    SEND_FILES(text = "SEND_FILES"),
    SEND_MESSAGE(text = "SEND_MESSAGE"),
    ADD_DESTINATION(text = "ADD_DESTINATION"),
    ;

    companion object {
        fun String.toCommandFlag() = entries.find { it.text == this }
    }
}
