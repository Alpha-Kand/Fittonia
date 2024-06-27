import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.error_add_destination_already_exists
import org.jetbrains.compose.resources.getString

enum class FittoniaErrorType {
    // Command specific.
    SET_AND_RESET_DEFAULT_PORT, // Set Default Port Command.
    TOO_MANY_SEARCH_TERMS, // Help Command

    // Command validation.
    INVALID_NUM_OF_COMMANDS,
    COMMAND_DOESNT_TAKE_THIS_ARGUMENT,

    // Argument validation.
    REQUIRED_ARGUMENT_NOT_FOUND,
    INVALID_ARGUMENT,
    DUPLICATE_ARGUMENT,
    ARGUMENT_DOESNT_TAKE_VALUE,

    // Port number validation.
    NON_NUMERICAL_PORT,
    PORT_NUM_OUT_OF_RANGE,

    // Sending validation.
    JOB_NAME_ILLEGAL_CHARACTERS,
    CANT_COLLECT_FILES_TWICE,
    CANT_SEND_MESSAGE_TWICE,

    // Settings.
    DESTINATION_NOT_FOUND,
    ENCRYPTION_ERROR,
    DECRYPTION_ERROR,
    ADD_DESTINATION_ALREADY_EXISTS,
}

class FittoniaError(
    private val errorType: FittoniaErrorType,
    error: Exception? = null,
    vararg values: Any,
) : Exception(error?.message) {
    private val errorValues = values

    constructor(
        errorType: FittoniaErrorType,
        vararg values: Any,
    ) : this(errorType = errorType, error = null, values = values)

    suspend fun getErrorMessage(): String {
        return getFittioniaErrorMessage().format(*errorValues.map { it.toString() }.toTypedArray())
    }

    private suspend fun getFittioniaErrorMessage() = when (this.errorType) {
        FittoniaErrorType.SET_AND_RESET_DEFAULT_PORT -> "Cannot set and reset default port at the same time."
        FittoniaErrorType.JOB_NAME_ILLEGAL_CHARACTERS -> "Job name can only contain letters and digits."
        FittoniaErrorType.CANT_COLLECT_FILES_TWICE -> "Tried to collect files to send more than once."
        FittoniaErrorType.DESTINATION_NOT_FOUND -> "No registered destination found with the name: %s"
        FittoniaErrorType.ARGUMENT_DOESNT_TAKE_VALUE -> "This argument does not take a value: %s"
        FittoniaErrorType.INVALID_NUM_OF_COMMANDS -> "Invalid number of commands detected: %s"
        FittoniaErrorType.INVALID_ARGUMENT -> "Invalid parameter: %s"
        FittoniaErrorType.REQUIRED_ARGUMENT_NOT_FOUND -> "Required argument was not found: %s"
        FittoniaErrorType.COMMAND_DOESNT_TAKE_THIS_ARGUMENT -> "This command does not take this argument: %s"
        FittoniaErrorType.DUPLICATE_ARGUMENT -> "Duplicate argument found: %s"
        FittoniaErrorType.NON_NUMERICAL_PORT -> "Non-numerical port or out of bounds: %s"
        FittoniaErrorType.PORT_NUM_OUT_OF_RANGE -> "Given port out of range (%s-%s): %s"
        FittoniaErrorType.ENCRYPTION_ERROR -> "Error while encrypting: %s"
        FittoniaErrorType.DECRYPTION_ERROR -> "Error while decrypting: %s"
        FittoniaErrorType.ADD_DESTINATION_ALREADY_EXISTS -> getString(Res.string.error_add_destination_already_exists)
        FittoniaErrorType.CANT_SEND_MESSAGE_TWICE -> "Tried to set message more than once."
        FittoniaErrorType.TOO_MANY_SEARCH_TERMS -> "Cannot search with both terms at the same time."
    }
}

suspend fun Session.reportFittoniaError2(e: FittoniaError) {
    e.getErrorMessage().let {
        section {
            red { text("Error: ") }
            textLine(it)
        }.run()
    }
}
