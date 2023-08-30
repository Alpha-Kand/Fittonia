fun <T> requireNull(value: T?) {
    if(value != null) {
        throw IllegalStateException("Required value was NOT null.")
    }
}