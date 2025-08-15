package org.hmeadow.fittonia

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import recordThrowable
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        recordThrowable(throwable = throwable)
        println("BaseViewModel error.message: ${throwable.message}")
        println("BaseViewModel error.cause: ${throwable.cause}")
    }
}
