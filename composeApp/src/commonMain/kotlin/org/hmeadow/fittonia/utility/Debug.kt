package org.hmeadow.fittonia.utility

import isDebug

fun debug(releaseBlock: () -> Unit = {}, debugBlock: () -> Unit) {
    if (isDebug()) {
        debugBlock()
    } else {
        releaseBlock()
    }
}

fun <T> debug(releaseValue: T, debugValue: T): T {
    return if (isDebug()) {
        debugValue
    } else {
        releaseValue
    }
}
