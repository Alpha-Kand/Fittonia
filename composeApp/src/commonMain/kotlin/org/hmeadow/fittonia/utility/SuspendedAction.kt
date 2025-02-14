package org.hmeadow.fittonia.utility

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Encapsulates a suspend block launched in the given scope, typically the local coroutine scope.
 * Various composables can monitor the [isRunning] var to update according to the suspended action's execution state.
 *
 * Note how [SuspendedAction] inherits from the () -> Unit, allowing it to seamlessly be called in place of any
 * callback.
 */
class SuspendedAction(
    private val suspendBlock: suspend () -> Unit,
    private val scope: CoroutineScope,
) : () -> Unit {

    var isRunning by mutableStateOf(value = false)
        private set

    override fun invoke() {
        if (!isRunning) {
            scope.launch(Dispatchers.Main) {
                isRunning = true
                suspendBlock()
                isRunning = false
            }
        }
    }
}

/**
 * Encapsulates a suspend block launched in the given scope, typically the local coroutine scope.
 * Various composables can monitor the [isRunning] var to update according to the suspended action's execution state.
 *
 * Note how [SuspendedActionTyped] inherits from the (T) -> Unit, allowing it to seamlessly be called in place of any
 * callback.
 */
class SuspendedActionTyped<T>(
    private val suspendBlock: suspend (T) -> Unit,
    private val scope: CoroutineScope,
) : (T) -> Unit {

    var isRunning by mutableStateOf(value = false)
        private set

    override fun invoke(arg: T) {
        if (!isRunning) {
            scope.launch(Dispatchers.Main) {
                isRunning = true
                suspendBlock(arg)
                isRunning = false
            }
        }
    }
}
