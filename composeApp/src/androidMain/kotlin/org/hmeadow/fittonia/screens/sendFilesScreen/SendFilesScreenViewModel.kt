package org.hmeadow.fittonia.screens.sendFilesScreen

import SettingsManager
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
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
import org.hmeadow.fittonia.utility.ContinueFlow
import org.hmeadow.fittonia.utility.DestinationPing
import org.hmeadow.fittonia.utility.canContinue
import org.hmeadow.fittonia.utility.debug
import org.hmeadow.fittonia.utility.decodeIpAddress
import org.hmeadow.fittonia.utility.getFileSizeBytes
import org.hmeadow.fittonia.utility.tryOrNull

internal class SendFilesScreenViewModel(
    private val onSaveOneTimeDestinationCallback: (
        ip: String,
        accessCode: String,
        onFinish: (newDestination: SettingsManager.Destination) -> Unit,
    ) -> Unit,
    private val onAddNewDestinationCallback: (onFinish: (newDestination: SettingsManager.Destination) -> Unit) -> Unit,
    private val onPing: suspend (ip: String, port: Int, accessCode: String, requestTimestamp: Long) -> Ping,
    private val onConfirmCallback: suspend (OutgoingJob) -> Unit,
) : BaseViewModel(), DestinationPing {
    val itemListState = MutableStateFlow<List<TransferJob.Item>>(emptyList())
    val itemListContinue: ContinueFlow<List<TransferJob.Item>> = ContinueFlow(flow = itemListState) { itemList ->
        if (itemList.isNotEmpty()) {
            ContinueFlow.ContinueFlag.Pass
        } else {
            ContinueFlow.ContinueFlag.Fail
        }
    }
    val selectedDestinationState = MutableStateFlow<SettingsManager.Destination?>(null)
    val selectedDestinationContinue = ContinueFlow(flow = selectedDestinationState) { itemList ->
        if (itemList != null) {
            ContinueFlow.ContinueFlag.Pass
        } else {
            ContinueFlow.ContinueFlag.Fail
        }
    }
    val portState: InputFlow = initInputFlow(
        initial = debug(debugValue = "44556", releaseValue = "44556"), // TODO
        onValueChange = { port ->
            if (port.isNotEmpty()) {
                selectedDestinationState.value?.let {
                    updatePing(it, port.toInt(), onPing = onPing)
                } ?: updatePing(
                    ip = oneTimeIpAddressState.text,
                    accessCode = oneTimeAccessCodeState.text,
                    port = port.toInt(),
                    onPing = onPing,
                )
            }
        },
    )
    override val ping = MutableStateFlow(value = Ping(PingStatus.NoPing))
    val descriptionState: InputFlow = initInputFlow(initial = "")

    val equivelentIpOrCode: MutableStateFlow<EquivalentIPCode> = MutableStateFlow(value = EquivalentIPCode.Neither)

    val oneTimeIpAddressState: InputFlow = initInputFlow(
        initial = "",
        onValueChange = { ip ->
            updatePing(
                ip = ip,
                accessCode = oneTimeAccessCodeState.text,
                port = portState.text.toInt(),
                onPing = onPing,
            )
            equivelentIpOrCode.update { decipherIpAndCode(ip = ip) }
        },
    )
    val oneTimeIpAddressContinue: ContinueFlow<String> = ContinueFlow(flow = oneTimeIpAddressState) { ipAddress ->
        if (ipAddress.isNotEmpty() && decipherIpAndCode(ipAddress) !is EquivalentIPCode.Neither) {
            ContinueFlow.ContinueFlag.Pass
        } else {
            ContinueFlow.ContinueFlag.Fail
        }
    }
    val oneTimeAccessCodeState: InputFlow = initInputFlow(
        initial = "",
        onValueChange = { accessCode ->
            if (portState.text.isNotBlank()) {
                updatePing(
                    ip = oneTimeIpAddressState.text,
                    accessCode = accessCode,
                    port = portState.text.toInt(),
                    onPing = onPing,
                )
            }
        },
    )
    val oneTimeAccessCodeContinue: ContinueFlow<String> = ContinueFlow(flow = oneTimeAccessCodeState) { accessCode ->
        if (accessCode.isNotEmpty()) {
            ContinueFlow.ContinueFlag.Pass
        } else {
            ContinueFlow.ContinueFlag.Fail
        }
    }

    val canContinue = combine(
        itemListContinue.result,
        selectedDestinationContinue.result,
        portState,
        ping,
    ) { itemList, destination, port, pingStatus ->
        itemList.canContinue
            .and(port.isNotEmpty())
            .and(destination.canContinue)
            .and(pingStatus.isSuccessful)
    }

    val canContinueOneTime = combine(
        itemListContinue.result,
        oneTimeIpAddressContinue.result,
        oneTimeAccessCodeContinue.result,
        portState,
        ping,
    ) { itemList, ip, accessCode, port, pingStatus ->
        itemList.canContinue
            .and(ip.canContinue)
            .and(port.isNotEmpty())
            .and(accessCode.canContinue)
            .and(pingStatus.isSuccessful)
    }

    init {
        launchInputFlows()
    }

    fun updateDestination(destination: SettingsManager.Destination) {
        selectedDestinationState.value = destination
        if (portState.text.isNotEmpty()) {
            updatePing(destination = destination, portState.text.toInt(), onPing = onPing)
        }
    }

    fun onSaveOneTimeDestinationClicked() {
        onSaveOneTimeDestinationCallback(oneTimeIpAddressState.text, oneTimeAccessCodeState.text) { newDestination ->
            selectedDestinationState.value = newDestination
            if (portState.text.isNotEmpty()) {
                updatePing(destination = newDestination, port = portState.text.toInt(), onPing = onPing)
            }
        }
    }

    fun onAddNewDestinationClicked() {
        onAddNewDestinationCallback { newDestination ->
            selectedDestinationState.value = newDestination
            if (portState.text.isNotEmpty()) {
                updatePing(destination = newDestination, port = portState.text.toInt(), onPing = onPing)
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
