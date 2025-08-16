package org.hmeadow.fittonia.utility

inline fun <reified T> T.printMe(prefix: String = ""): T {
    if (prefix.isNotEmpty()) {
        print(prefix)
    }
    println(this)
    return this
}
