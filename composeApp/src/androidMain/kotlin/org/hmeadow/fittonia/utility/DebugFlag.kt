package org.hmeadow.fittonia.utility

import androidx.compose.runtime.Composable
import org.hmeadow.fittonia.BuildConfig

fun isDebug() = BuildConfig.DEBUG

fun debug(block: () -> Unit) {
    if (isDebug()) {
        block()
    }
}

@Composable
fun Debug(releaseBlock: @Composable () -> Unit = {}, debugBlock: @Composable () -> Unit ) {
    if (isDebug()) {
        debugBlock()
    } else {
        releaseBlock()
    }
}
