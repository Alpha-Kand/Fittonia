package org.hmeadow.fittonia.utility

import androidx.compose.runtime.Composable
import isDebug
import org.hmeadow.fittonia.utility.DebugTurnOff.allowDebug

object DebugTurnOff {
    var allowDebug = true
}

sealed interface Switch
data object On : Switch
data object Off : Switch

val Switch.isOn: Boolean
    get() = this is On

@Composable
fun Debug(debug: Switch = On, releaseBlock: @Composable () -> Unit = {}, debugBlock: @Composable () -> Unit) {
    if (debug.isOn) {
        if (isDebug() && allowDebug) {
            debugBlock()
        } else {
            releaseBlock()
        }
    }
}
