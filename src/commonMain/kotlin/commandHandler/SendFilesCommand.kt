package commandHandler

import requireNull

object SendFilesCommand : SendCommand(), Command {
    private var files: List<String>? = null
    private var job: String? = null

    fun getFiles() = verifyArgumentIsSet(argument = files, reportingName = filesArguments.first())
    fun getJob() = job

    override fun verify() {
        super.verify()
        getJob()?.let { jobName ->
            if (!jobName.all { it.isLetterOrDigit() }) {
                throw IllegalArgumentException("Job name can only contain letters and digits.")
            }
        }
        getFiles()
    }

    fun setFiles(newFiles: List<String>) {
        if (newFiles.isNotEmpty()) {
            files = newFiles
        } else {
            throw IllegalStateException("Tried to collect files to send more than once.")
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
