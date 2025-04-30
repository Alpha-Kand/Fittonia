package org.hmeadow.fittonia.utility

import androidx.compose.runtime.Composable
import isDebug
import org.hmeadow.fittonia.utility.DebugTurnOff.allowDebug

object DebugTurnOff {
    var allowDebug = true
}

@Composable
fun Debug(releaseBlock: @Composable () -> Unit = {}, debugBlock: @Composable () -> Unit) {
    if (isDebug() && allowDebug) {
        debugBlock()
    } else {
        releaseBlock()
    }
}
