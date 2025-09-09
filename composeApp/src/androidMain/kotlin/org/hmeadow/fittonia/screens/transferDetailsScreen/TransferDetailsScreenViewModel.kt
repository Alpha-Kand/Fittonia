package org.hmeadow.fittonia.screens.transferDetailsScreen

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.androidServer.AndroidServer.Companion.server
import org.hmeadow.fittonia.models.TransferJob

internal class TransferDetailsScreenViewModel(private val transferJob: TransferJob) : BaseViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentTransferJob: Flow<TransferJob?> = server.flatMapLatest { androidServer ->
        androidServer?.transferJobs?.map { transferJobs ->
            transferJobs.firstOrNull { it.id == transferJob.id }
        } ?: flow { emit(null) }
    }
}
