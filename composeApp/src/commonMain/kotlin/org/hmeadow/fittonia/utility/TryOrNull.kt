package org.hmeadow.fittonia.utility

/**
 * Attempts to execute the given [block], but if an [Exception] is thrown, it is caught and a null value is returned.
 */
fun <T> tryOrNull(block: () -> T?): T? {
    return try {
        block()
    } catch (_: Exception) {
        null
    }
}
