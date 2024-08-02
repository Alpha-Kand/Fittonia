package org.hmeadow.fittonia

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
}
