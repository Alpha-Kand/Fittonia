package org.hmeadow.fittonia.utility

import androidx.compose.runtime.Composable
import isDebug

@Composable
fun Debug(releaseBlock: @Composable () -> Unit = {}, debugBlock: @Composable () -> Unit ) {
    if (isDebug()) {
        debugBlock()
    } else {
        releaseBlock()
    }
}
