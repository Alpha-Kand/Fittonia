package commandHandler

import FittoniaError
import FittoniaErrorType
import requireNull

class SendFilesCommand : SendCommand(), Command {
    private var files: List<String>? = null
    private var job: String? = null

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
        if (handleSendCommandArgument(argumentName = argumentName, value = value)) {
            return@tryCatch true
        }
        if (jobArguments.contains(argumentName)) {
            requireNull(job)
            job = value
            return@tryCatch true
        }
        if (filesArguments.contains(argumentName)) {
            return@tryCatch true
        }
        return@tryCatch false
    }
}
