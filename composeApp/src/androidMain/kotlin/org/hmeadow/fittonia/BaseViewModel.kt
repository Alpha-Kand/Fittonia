package org.hmeadow.fittonia

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.compose.components.InputFlowCollectionLauncher
import org.hmeadow.fittonia.utility.debug
import recordThrowable
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : CoroutineScope {
    val inputFlowCollectionLauncher = InputFlowCollectionLauncher()

    private class ShutdownCoroutinesException : CancellationException()

    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        if (throwable !is ShutdownCoroutinesException) {
            recordThrowable(throwable = throwable)
            debug {
                println("BaseViewModel error.message: ${throwable.message}")
                println("BaseViewModel error.cause: ${throwable.cause}")
            }
        }
    }

    fun initInputFlow(initial: String, onValueChange: ((String) -> Unit)? = null): InputFlow {
        return InputFlow(initial = initial, onValueChange, inputFlowCollectionLauncher)
    }

    fun launchInputFlows() {
        inputFlowCollectionLauncher.launch()
    }

    fun shutdown() {
        coroutineContext.cancel(ShutdownCoroutinesException())
    }

    init {
        launch {
            delay(timeMillis = 3000)
            if (!inputFlowCollectionLauncher.hasLaunched && inputFlowCollectionLauncher.hasCallbacks) {
                throw IllegalStateException(
                    "ViewModel did not launch input flow collections: ${Navigator.currentScreenName}",
                )
            }
        }
    }
}
