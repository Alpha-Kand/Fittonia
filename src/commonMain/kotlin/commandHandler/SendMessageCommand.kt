package commandHandler

import FittoniaError
import FittoniaErrorType
import commandHandler.Command.Companion.verifyArgumentIsSet

class SendMessageCommand(
    private var message: String? = null,
) : SendCommand() {

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
        return@tryCatch messageArguments.contains(argumentName)
    }
}
