package org.hmeadow.fittonia.utility

import java.text.DecimalFormat

fun bytesToMegaBytes(bytes: Long): String {
    return DecimalFormat("#.##").format((bytes / 1000f) / 1000f)
}
