package commandHandler

import FittoniaError
import FittoniaErrorType
import commandHandler.Command.Companion.verifyArgumentIsSet
import requireNull

class SendFilesCommand(
    private var files: List<String>? = null,
    private var job: String? = null,
) : SendCommand(), MachineReadableOutput {
    override var ioFormat: Boolean = machineReadableDefault()

    fun getFiles() = verifyArgumentIsSet(argument = files, reportingName = filesArguments.first())
    fun getJob() = job

    override fun verify() {
        super.verify()
        getJob()?.let { jobName ->
            if (!jobName.all { it.isLetterOrDigit() }) {
                throw FittoniaError(FittoniaErrorType.JOB_NAME_ILLEGAL_CHARACTERS)
            }
        }
        getFiles()
    }

    fun setFiles(newFiles: List<String>) {
        if (newFiles.isNotEmpty()) {
            files = newFiles
        } else {
            throw FittoniaError(FittoniaErrorType.CANT_COLLECT_FILES_TWICE)
        }
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (handleMachineReadableOutputFlag(argumentName = argumentName)) {
            return@tryCatch true
        }
        if (handleSendCommandArgument(argumentName = argumentName, value = value)) {
            return@tryCatch true
        }
        if (jobArguments.contains(argumentName)) {
            requireNull(job)
            job = value
            return@tryCatch true
        }
        return@tryCatch filesArguments.contains(argumentName)
    }
}
