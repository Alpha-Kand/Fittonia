package commandHandler

import requireNull

object SendFilesCommand : Command {
    private var port: String? = null
    private var destination: String? = null
    private var files: List<String>? = null
    private var ip: String? = null
    private var password: String? = null
    private var job: String? = null

    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first()).toInt()
    fun getDestination() = destination
    fun getFiles() = verifyArgumentIsSet(argument = files, reportingName = filesArguments.first())
    fun getIP() = verifyArgumentIsSet(argument = ip, reportingName = ipArguments.first())
    fun getPassword() = verifyArgumentIsSet(argument = password, reportingName = passwordArguments.first())
    fun getJob() = job

    override fun verify() {
        if (destination == null) {
            getIP()
            getPassword()
        }
        getJob()?.let { jobName ->
            if (!jobName.all { it.isLetterOrDigit() }) {
                throw IllegalArgumentException("Job name can only contain letters and digits.")
            }
        }
        getPort()
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
        if (portArguments.contains(argumentName)) {
            requireNull(port)
            port = value
            return@tryCatch true
        }
        if (destinationArguments.contains(argumentName)) {
            requireNull(destination)
            destination = value
            return@tryCatch true
        }
        if (passwordArguments.contains(argumentName)) {
            requireNull(password)
            password = value
            return@tryCatch true
        }
        if (jobArguments.contains(argumentName)) {
            requireNull(job)
            job = value
            return@tryCatch true
        }
        if (ipArguments.contains(argumentName)) {
            requireNull(ip)
            ip = value
            return@tryCatch true
        }
        if (filesArguments.contains(argumentName)) {
            return@tryCatch true
        }
        return@tryCatch false
    }
}
