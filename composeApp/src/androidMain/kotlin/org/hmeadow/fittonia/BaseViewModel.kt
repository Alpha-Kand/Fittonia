package org.hmeadow.fittonia

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        println("BaseViewModel error.message: ${throwable.message}") // TODO handle errors, crashlytics? before release
        println("BaseViewModel error.cause: ${throwable.cause}")
    }
}
