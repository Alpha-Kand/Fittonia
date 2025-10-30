package org.hmeadow.fittonia.screens.sendFilesScreen

import SettingsManager
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.components.EquivalentIPCode
import org.hmeadow.fittonia.components.decipherIpAndCode
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.mainActivity.MainActivity
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus
import org.hmeadow.fittonia.models.mostRecent
import org.hmeadow.fittonia.utility.debug
import org.hmeadow.fittonia.utility.decodeIpAddress
import org.hmeadow.fittonia.utility.getFileSizeBytes
import org.hmeadow.fittonia.utility.tryOrNull
import java.time.Instant

internal class SendFilesScreenViewModel(
    private val onSaveOneTimeDestinationCallback: (
        ip: String,
        accessCode: String,
        onFinish: (newDestination: SettingsManager.Destination) -> Unit,
    ) -> Unit,
    private val onAddNewDestinationCallback: (onFinish: (newDestination: SettingsManager.Destination) -> Unit) -> Unit,
    private val onPing: suspend (ip: String, port: Int, accessCode: String, requestTimestamp: Long) -> Ping,
    private val onConfirmCallback: suspend (OutgoingJob) -> Unit,
) : BaseViewModel() {
    val itemListState = MutableStateFlow<List<TransferJob.Item>>(emptyList())
    val selectedDestinationState = MutableStateFlow<SettingsManager.Destination?>(null)
    val portState: InputFlow = initInputFlow(
        initial = debug(debugValue = "44556", releaseValue = "44556"), // TODO
        onValueChange = { port ->
            if (port.isNotEmpty()) {
                selectedDestinationState.value?.let {
                    updatePing(it, port.toInt())
                } ?: updatePing(
                    ip = oneTimeIpAddressState.text,
                    accessCode = oneTimeAccessCodeState.text,
                    port = port.toInt(),
                )
            }
        },
    )
    val descriptionState: InputFlow = initInputFlow(initial = "")

    val equivelentIpOrCode: MutableStateFlow<EquivalentIPCode> = MutableStateFlow(value = EquivalentIPCode.Neither)

    val oneTimeIpAddressState: InputFlow = initInputFlow(
        initial = "",
        onValueChange = { ip ->
            updatePing(
                ip = ip,
                accessCode = oneTimeAccessCodeState.text,
                port = portState.text.toInt(),
            )
            equivelentIpOrCode.update { decipherIpAndCode(ip = ip) }
        },
    )
    val oneTimeAccessCodeState: InputFlow = initInputFlow(
        initial = "",
        onValueChange = { accessCode ->
            if (portState.text.isNotBlank()) {
                updatePing(
                    ip = oneTimeIpAddressState.text,
                    accessCode = accessCode,
                    port = portState.text.toInt(),
                )
            }
        },
    )

    val canContinue = combine(
        itemListState,
        selectedDestinationState,
        portState,
    ) { itemList, _, port ->
        itemList.isNotEmpty() && port.isNotEmpty()
    }

    init {
        launchInputFlows()
    }

    fun updateDestination(destination: SettingsManager.Destination) {
        selectedDestinationState.value = destination
        if (portState.text.isNotEmpty()) {
            updatePing(destination, portState.text.toInt())
        }
    }

    private fun updatePing(destination: SettingsManager.Destination, port: Int) {
        updatePing(ip = destination.ip, accessCode = destination.accessCode, port = port)
    }

    private fun updatePing(ip: String, accessCode: String, port: Int) {
        if (ip.isNotBlank() && accessCode.isNotBlank()) {
            launch {
                val timestamp = Instant.now().toEpochMilli()
                updatePingAtomically(newPing = Ping(PingStatus.Processing, timestamp))
                updatePingAtomically(
                    newPing = onPing(
                        ip,
                        port, // TODO remove Port type. - After release
                        accessCode, // TODO before release - check if access code should be string and not bytearray.
                        Instant.now().toEpochMilli().let { now ->
                            if (now == timestamp) {
                                timestamp + 1
                            } else {
                                now
                            }
                        },
                    ),
                )
            }
        }
    }

    val ping = MutableStateFlow(value = Ping(PingStatus.NoPing))
    private val pingMutex = Mutex()
    private suspend fun updatePingAtomically(newPing: Ping) {
        pingMutex.withLock {
            ping.value = mostRecent(ping.value, newPing)
        }
    }

    val canContinueOneTime = combine(
        itemListState,
        oneTimeIpAddressState,
        oneTimeAccessCodeState,
        portState,
        ping,
    ) { itemList, ip, accessCode, port, pingStatus ->
        itemList.isNotEmpty()
            .and(ip.isNotEmpty())
            .and(port.isNotEmpty())
            .and(accessCode.isNotEmpty())
            .and(pingStatus.pingStatus is PingStatus.Success)
    }

    fun onSaveOneTimeDestinationClicked() {
        onSaveOneTimeDestinationCallback(oneTimeIpAddressState.text, oneTimeAccessCodeState.text) { newDestination ->
            selectedDestinationState.value = newDestination
            if (portState.text.isNotEmpty()) {
                updatePing(destination = newDestination, port = portState.text.toInt())
            }
        }
    }

    fun onAddNewDestinationClicked() {
        onAddNewDestinationCallback { newDestination ->
            selectedDestinationState.value = newDestination
            if (portState.text.isNotEmpty()) {
                updatePing(destination = newDestination, port = portState.text.toInt())
            }
        }
    }

    fun onUserSelectItem() {
        MainActivity.mainActivity.openFilePicker { selectedUri ->
            DocumentFile.fromSingleUri(MainActivity.mainActivity, selectedUri)?.let { docUri ->
                docUri.name?.let { name ->
                    itemListState.value += TransferJob.Item(
                        name = name,
                        uriRaw = docUri.uri.toString(),
                        isFile = docUri.isFile,
                        sizeBytes = MainActivity
                            .mainActivity
                            .getFileSizeBytes(uri = docUri.uri),
                    )
                } ?: run { /* TODO - After release */ }
            }
        }
    }

    suspend fun onConfirmClicked() {
        val newDescription = descriptionState.text.trim()
        onConfirmCallback(
            OutgoingJob(
                id = -1,
                description = newDescription,
                needDescription = descriptionState.text.isEmpty(),
                destination = selectedDestinationState.value ?: SettingsManager.Destination(
                    name = "-",
                    ip = oneTimeIpAddressState.text.let { tryOrNull { decodeIpAddress(ipAddress = it) } ?: it },
                    accessCode = oneTimeAccessCodeState.text,
                ), // TODO - After release
                items = itemListState.value,
                port = portState.text.toInt(),
                status = TransferStatus.Sending,
                bytesPerSecond = 0,
            ),
        )
    }
}
