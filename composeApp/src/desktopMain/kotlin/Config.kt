object Config {

    object OSMapper {
        private enum class OSType {
            LINUX,
            MACOS,
            WINDOWS,
        }

        private val osType = OSType.LINUX

        val settingsOSSpecificPath = when (osType) {
            OSType.LINUX -> "/home/hunterneo/Desktop/TRANSFER/fittoniaSettings|5.xml"
            OSType.MACOS -> "/Users/hunter.wiesman/Desktop/FittoniaTRANSFER/fittoniaSettings|5.xml"
            OSType.WINDOWS -> ""
        }
    }
}
