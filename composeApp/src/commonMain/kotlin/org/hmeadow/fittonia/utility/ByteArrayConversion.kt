package org.hmeadow.fittonia.utility

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

fun <T> convertToJSONByteArray(data: T): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    jacksonObjectMapper().writeValue(byteArrayOutputStream, data)
    return byteArrayOutputStream.toByteArray()
}

val <T> T.toJSONByteArray: ByteArray
    get() = convertToJSONByteArray(this)

val ByteArray.toString: String
    get() = String(bytes = this, charset = StandardCharsets.UTF_8)

inline fun <reified T> convertToObject(data: ByteArray): T = jacksonObjectMapper().readValue<T>(data.toString)

val Int.byteArray: ByteArray
    get() = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
