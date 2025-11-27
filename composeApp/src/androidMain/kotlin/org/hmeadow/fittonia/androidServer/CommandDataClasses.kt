package org.hmeadow.fittonia.androidServer

import org.hmeadow.fittonia.models.TransferJob.Item

class PingClientData(
    val accessCode: String,
)

class PingServerData(
    val isAccessCodeCorrect: Boolean,
)

class SendFileClientData(
    val items: List<Item>,
    val aesKey: ByteArray,
    val jobName: String?,
    val accessCode: String,
)

class SendFileServerData(
    val jobName: String,
    val pathLimit: Int,
    val isAccessCodeCorrect: Boolean,
)
