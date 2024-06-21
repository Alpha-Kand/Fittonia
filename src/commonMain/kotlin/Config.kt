import com.varabyte.kotter.foundation.text.Color

object Config {
    val IS_MOCKING = false

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

data object OutputIO {
    private val outputIO = mutableListOf<String>()

    fun printlnIO(text: String, color: Color = Color.WHITE) {
        if (Config.IS_MOCKING) {
            outputIO.add(text)
        } else {
            printLine(text, color)
        }
    }

    fun printlnIO() {
        if (Config.IS_MOCKING) {
            outputIO.add("\n")
        } else {
            printLine("\n")
        }
    }

    fun flush(): List<String> = outputIO.toList().also { outputIO.clear() }
}
