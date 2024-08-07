import kotlinx.coroutines.runBlocking

fun <T> requireNull(value: T?) {
    if (value != null) {
        throw IllegalStateException("Required value was NOT null.")
    }
}

fun <T> Iterable<T>.forEachSuspended(action: suspend (T) -> Unit) {
    this.forEach {
        runBlocking {
            action(it)
        }
    }
}

inline fun Boolean.alsoIfTrue(block: () -> Unit): Boolean {
    if (this) {
        block()
    }
    return this
}
