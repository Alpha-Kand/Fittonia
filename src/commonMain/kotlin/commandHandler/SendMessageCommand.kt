package commandHandler

import FittoniaError
import FittoniaErrorType

class SendMessageCommand : SendCommand(), Command {
    private var message: String? = null

    fun getMessage() = verifyArgumentIsSet(argument = message, reportingName = messageArguments.first())

    fun setMessage(input: String) {
        if (message == null) {
            message = input
        } else {
            throw FittoniaError(FittoniaErrorType.CANT_SEND_MESSAGE_TWICE)
        }
    }

    override fun verify() {
        super.verify()
        getMessage()
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (handleSendCommandArgument(argumentName = argumentName, value = value)) {
            return@tryCatch true
        }
        if (messageArguments.contains(argumentName)) {
            return@tryCatch true
        }
        return@tryCatch false
    }
}
