object Config {
    const val IS_MOCKING = false

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
            OSType.LINUX -> "/home/hunterneo/Desktop/TRANSFER/fittoniaSettings|3.xml"
            OSType.MACOS -> "/Users/hunter.wiesman/Desktop/FittoniaTRANSFER/fittoniaSettings|3.xml"
            OSType.WINDOWS -> ""
        }
    }
}
