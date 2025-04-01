package org.hmeadow.fittonia.models

import SettingsManager
import android.net.Uri
import org.hmeadow.fittonia.models.TransferJob.Item
import java.util.Objects
import kotlin.math.min

sealed interface TransferJob {
    val id: Int
    val status: TransferStatus
    val description: String
    val currentItem: Int
    val items: List<Item>
    val totalItems: Int
        get() = items.size
    val progressPercentage: Double
        get() = items
            .takeIf { it.isNotEmpty() }
            ?.sumOf { it.progressBytes }
            ?.toDouble()
            ?.div(items.sumOf { it.sizeBytes }.toDouble()) ?: 0.0
    val nextItem: Int
        get() = min(currentItem + 1, totalItems)

    fun cloneItems(): List<Item> = items.map { it.copy() }
    fun getUpdatedItemList(item: Item): List<Item> {
        val filteredList = items.filter { it.id != item.id }
        return if (filteredList.size != items.size) {
            filteredList.plus(item)
        } else {
            items
        }
    }

    data class Item(
        val uriRaw: String,
        val name: String,
        val isFile: Boolean,
        val sizeBytes: Long,
        val progressBytes: Long = 0,
    ) {
        fun uri(): Uri = Uri.parse(uriRaw)
        val progressPercentage: Double = progressBytes.toDouble() / sizeBytes.toDouble()
        val id: Int = Objects.hash(name, uriRaw, isFile, sizeBytes) // Ignore 'progressBytes'.
    }
}

data class OutgoingJob(
    override val id: Int,
    override val status: TransferStatus,
    override val description: String,
    override val currentItem: Int = 1,
    override val items: List<Item>,
    val port: Int,
    val destination: SettingsManager.Destination,
    val needDescription: Boolean,
) : TransferJob {
    fun updateItem(item: Item): OutgoingJob = this.copy(items = getUpdatedItemList(item))
}

data class IncomingJob(
    override val id: Int,
    override val status: TransferStatus,
    override val description: String,
    override val currentItem: Int = 1,
    override val items: List<Item>,
    val source: Source,
) : TransferJob {
    constructor(id: Int) : this(
        id = id,
        status = TransferStatus.Receiving,
        description = "",
        currentItem = 0,
        items = emptyList(),
        source = Source(),
    )

    fun updateItem(item: Item): IncomingJob = this.copy(items = getUpdatedItemList(item))

    data class Source(
        val name: String,
        val ip: String,
    ) {
        constructor() : this(name = "", ip = "")
    }
}

enum class TransferStatus {
    Sending,
    Receiving,
    Error,
    Done,
}
