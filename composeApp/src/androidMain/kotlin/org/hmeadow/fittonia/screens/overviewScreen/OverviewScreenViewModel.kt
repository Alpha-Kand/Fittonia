package org.hmeadow.fittonia.screens.overviewScreen

import SettingsManager
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.androidServer.AndroidServer
import org.hmeadow.fittonia.mainActivity.MainActivity
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus
import org.hmeadow.fittonia.utility.createJobDirectory
import kotlin.math.abs
import kotlin.random.Random

internal class OverviewScreenViewModel(
    private val onUpdateDumpPath: (Uri) -> Unit,
) : BaseViewModel() {
    val needDumpAccess = MutableStateFlow(false)
    val deviceIp = MutableStateFlow("Unknown")

    init {
        launch {
            when (val state = MainActivity.mainActivity.createJobDirectory(jobName = "ACCESS")) {
                is MainActivity.CreateDumpDirectory.Error -> {
                    needDumpAccess.update { true }
                    MainActivity.mainActivity.alert(UserAlert.DumpLocationLost)
                }

                is MainActivity.CreateDumpDirectory.Success -> MainActivity.mainActivity.deleteDumpDirectory(state.uri)
            }
        }
        refreshIp()
    }

    fun refreshIp() {
        deviceIp.value = MainActivity.mainActivity.getDeviceIpAddress() ?: "Unknown"
    }

    fun onDumpPathPicked(path: Uri) {
        onUpdateDumpPath(path)
        needDumpAccess.update { false }
        MainActivity.mainActivity.unAlert<UserAlert.DumpLocationLost>()
    }

    // TODO Move to debug screen.
    fun addNewDebugJob() = launch {
        AndroidServer.server.value?.registerTransferJob(
            OutgoingJob(
                id = Random.nextInt(),
                description = "Sending PDFs to bob (${abs(Random.nextInt() % 100)})",
                needDescription = false,
                destination = SettingsManager.Destination(
                    name = "Bob's PC (${abs(Random.nextInt() % 100)})",
                    ip = "192.168.1.1",
                    accessCode = "accesscode",
                ),
                items = (0..abs(Random.nextInt() % 20)).map {
                    val totalSize = abs(Random.nextLong())
                    TransferJob.Item(
                        name = "File_${abs(Random.nextInt() % 100)}.pdf",
                        uriRaw = "https://www.google.com",
                        isFile = true,
                        sizeBytes = totalSize,
                        progressBytes = Random.nextLong(from = 0, until = totalSize),
                    )
                },
                port = 5556,
                status = TransferStatus.entries.random(),
                bytesPerSecond = 0,
            ),
        )
    }
}
