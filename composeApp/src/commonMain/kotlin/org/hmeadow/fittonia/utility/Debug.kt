package org.hmeadow.fittonia.utility

import isDebug

fun debug(releaseBlock: () -> Unit = {}, debugBlock: () -> Unit) {
    if (isDebug()) {
        debugBlock()
    } else {
        releaseBlock()
    }
}
