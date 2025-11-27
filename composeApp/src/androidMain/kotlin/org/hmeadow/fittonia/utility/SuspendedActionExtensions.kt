package org.hmeadow.fittonia.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.monotonicFrameClock
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.plus
import org.hmeadow.fittonia.BaseViewModel

@OptIn(ExperimentalComposeApi::class)
@Composable
fun BaseViewModel.rememberSuspendedAction(
    suspendBlock: suspend () -> Unit,
): SuspendedAction {
    val composeScope = rememberCoroutineScope()
    return remember(suspendBlock) {
        SuspendedAction(
            suspendBlock = suspendBlock,
            scope = this + composeScope.coroutineContext.monotonicFrameClock,
        )
    }
}

@OptIn(ExperimentalComposeApi::class)
@Composable
fun <T> BaseViewModel.rememberSuspendedAction(
    suspendBlock: suspend (T) -> Unit,
): SuspendedActionTyped<T> {
    val composeScope = rememberCoroutineScope()
    return remember(suspendBlock) {
        SuspendedActionTyped(
            suspendBlock = suspendBlock,
            scope = this + composeScope.coroutineContext.monotonicFrameClock,
        )
    }
}
