object Config {

    object OSMapper {
        private enum class OSType {
            LINUX,
            MACOS,
            WINDOWS,
        }

        private val osType = OSType.MACOS

        val settingsOSSpecificPath = when (osType) {
            OSType.LINUX -> "/home/hunterneo/Desktop/TRANSFER/fittoniaSettings|4.xml"
            OSType.MACOS -> "/Users/hunter.wiesman/Desktop/FittoniaTRANSFER/fittoniaSettings|4.xml"
            OSType.WINDOWS -> ""
        }
    }
}
