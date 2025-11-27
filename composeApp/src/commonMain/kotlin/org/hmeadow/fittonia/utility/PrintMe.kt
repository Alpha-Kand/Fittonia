package org.hmeadow.fittonia.utility

inline fun <reified T> T.printMe(prefix: String = ""): T {
    debug {
        if (prefix.isNotEmpty()) {
            print(prefix)
        }
        println(this)
    }
    return this
}
