package org.hmeadow.fittonia.androidServer

import org.hmeadow.fittonia.models.TransferJob.Item

class PingClientData(
    val password: String,
)

class PingServerData(
    val isPasswordCorrect: Boolean,
)

class SendFileClientData(
    val items: List<Item>,
    val aesKey: ByteArray,
    val jobName: String,
    val password: String,
    val nameFlag: String,
)

class SendFileServerData(
    val jobName: String,
    val pathLimit: Int,
    val isPasswordCorrect: Boolean,
)
