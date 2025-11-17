package org.hmeadow.fittonia.screens.transferDetailsScreen

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.androidServer.AndroidServer.Companion.server
import org.hmeadow.fittonia.mainActivity.MainViewModel
import org.hmeadow.fittonia.models.CompletedJob.Direction
import org.hmeadow.fittonia.models.IncomingJob
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.TransferStatus

internal class TransferDetailsScreenViewModel(
    private val id: Int,
    private val mainViewModel: MainViewModel,
) : BaseViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentTransferJob: Flow<TransferDetailsScreenData?> = server.flatMapLatest { androidServer ->
        combine(
            androidServer?.transferJobs ?: flow { emit(emptyList()) },
            mainViewModel.dataStore.data,
        ) { activeTransferJobs, settings ->
            activeTransferJobs.firstOrNull { it.id == id }?.let { transferJob ->
                TransferDetailsScreenData(
                    description = transferJob.description,
                    status = transferJob.status,
                    progressPercentage = transferJob.progressPercentage,
                    currentItem = transferJob.currentItem,
                    totalItems = transferJob.totalItems,
                    bytesPerSecond = transferJob.bytesPerSecond,
                    transferTarget = when (transferJob) {
                        is OutgoingJob -> TransferTarget.Out(destinationName = transferJob.destination.name)
                        is IncomingJob -> TransferTarget.In(sourceName = transferJob.source.name)
                    },
                    items = transferJob.items.map { aaa ->
                        TransferDetailsScreenDataItem(
                            name = aaa.name,
                            progressPercentage = aaa.progressPercentage,
                            sizeBytes = aaa.sizeBytes,
                            progressBytes = aaa.progressBytes,
                        )
                    },
                )
            } ?: settings.completedJobs.firstOrNull { completedJob -> completedJob.id == id }?.let { completedJob ->
                TransferDetailsScreenData(
                    description = completedJob.description,
                    status = TransferStatus.Done,
                    progressPercentage = 1.0,
                    currentItem = completedJob.items.size,
                    totalItems = completedJob.items.size,
                    bytesPerSecond = 0,
                    transferTarget = when (completedJob.direction) {
                        Direction.Incoming -> TransferTarget.In(sourceName = completedJob.targetName)
                        Direction.Outgoing -> TransferTarget.Out(destinationName = completedJob.targetName)
                    },
                    items = completedJob.items.map { completedItem ->
                        TransferDetailsScreenDataItem(
                            name = completedItem.name,
                            progressPercentage = 1.0,
                            sizeBytes = completedItem.sizeBytes,
                            progressBytes = completedItem.sizeBytes,
                        )
                    },
                )
            }
        }
    }
}
