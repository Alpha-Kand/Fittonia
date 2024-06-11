object Config {
    val IS_MOCKING = false

    object OSMapper {
        private enum class OSType {
            LINUX,
            MACOS,
            WINDOWS,
        }

        private val osType = OSType.MACOS
        private const val VERSION = "1.0"

        val clientEngineJar = when (osType) {
            OSType.LINUX -> "FittoniaClientEngine-linux-x64-$VERSION.jar"
            OSType.MACOS -> "FittoniaClientEngine-macos-arm64-$VERSION.jar"
            OSType.WINDOWS -> ""
        }

        val serverEngineJar = when (osType) {
            OSType.LINUX -> "FittoniaServerEngine-linux-x64-$VERSION.jar"
            OSType.MACOS -> "FittoniaServerEngine-macos-arm64-$VERSION.jar"
            OSType.WINDOWS -> ""
        }

        val settingsOSSpecificPath = when (osType) {
            OSType.LINUX -> "/home/hunterneo/Desktop/TRANSFER/fittoniaSettings|4.xml"
            OSType.MACOS -> "/Users/hunter.wiesman/Desktop/FittoniaTRANSFER/fittoniaSettings|4.xml"
            OSType.WINDOWS -> ""
        }
    }
}

data object OutputIO {
    private val outputIO = mutableListOf<String>()

    fun printlnIO(output: String) {
        if (Config.IS_MOCKING) {
            outputIO.add(output)
        } else {
            printLine(output)
        }
    }

    fun flush(): List<String> = outputIO.toList().also { outputIO.clear() }
}
