package org.hmeadow.fittonia.screens

import SettingsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.documentfile.provider.DocumentFile
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.BuildConfig
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaNumberInput
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.FittoniaTextInput
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.InputFlow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaLoadingIndicator
import org.hmeadow.fittonia.design.fonts.headingMStyle
import org.hmeadow.fittonia.design.fonts.inputLabelStyle
import org.hmeadow.fittonia.design.fonts.paragraphStyle
import org.hmeadow.fittonia.design.fonts.psstStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldLightTextStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldTextStyle
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus
import org.hmeadow.fittonia.models.mostRecent
import org.hmeadow.fittonia.screens.overviewScreen.Options
import org.hmeadow.fittonia.utility.rememberSuspendedAction
import java.time.Instant

class SendFilesScreenViewModel(
    private val onSaveOneTimeDestinationCallback: (
        ip: String,
        password: String,
        onFinish: (newDestination: SettingsManager.Destination) -> Unit,
    ) -> Unit,
    private val onAddNewDestinationCallback: (onFinish: (newDestination: SettingsManager.Destination) -> Unit) -> Unit,
    private val onPing: suspend (ip: String, port: Int, password: String, requestTimestamp: Long) -> Ping,
    private val onConfirmCallback: suspend (OutgoingJob) -> Unit,
) : BaseViewModel() {
    val itemListState = MutableStateFlow<List<TransferJob.Item>>(emptyList())
    val selectedDestinationState = MutableStateFlow<SettingsManager.Destination?>(null)
    val portState: InputFlow = InputFlow(
        initial = if (BuildConfig.DEBUG) "12345" else "",
        onValueChange = { port ->
            if (port.isNotEmpty()) {
                selectedDestinationState.value?.let {
                    updatePing(it, port.toInt())
                } ?: updatePing(
                    ip = oneTimeIpAddressState.text,
                    password = oneTimePasswordState.text,
                    port = port.toInt(),
                )
            }
        },
    )
    val descriptionState = InputFlow(initial = "")

    val oneTimeIpAddressState: InputFlow = InputFlow(
        initial = "",
        onValueChange = { ip ->
            updatePing(
                ip = ip,
                password = oneTimePasswordState.text,
                port = portState.text.toInt(),
            )
        },
    )
    val oneTimePasswordState = InputFlow(
        initial = "",
        onValueChange = { password ->
            updatePing(
                ip = oneTimeIpAddressState.text,
                password = password,
                port = portState.text.toInt(),
            )
        },
    )

    val canContinue = combine(
        itemListState,
        selectedDestinationState,
        portState,
    ) { itemList, _, port ->
        itemList.isNotEmpty() && port.isNotEmpty()
    }

    fun updateDestination(destination: SettingsManager.Destination) {
        selectedDestinationState.value = destination
        if (portState.text.isNotEmpty()) {
            updatePing(destination, portState.text.toInt())
        }
    }

    private fun updatePing(destination: SettingsManager.Destination, port: Int) {
        updatePing(ip = destination.ip, password = destination.password, port = port)
    }

    private fun updatePing(ip: String, password: String, port: Int) {
        if (ip.isNotBlank() && password.isNotBlank()) {
            launch {
                val timestamp = Instant.now().toEpochMilli()
                updatePingAtomically(newPing = Ping(PingStatus.Processing, timestamp))
                updatePingAtomically(
                    newPing = onPing(
                        ip,
                        port, // TODO make Port type.
                        password,
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

    val ping = MutableStateFlow(Ping(PingStatus.NoPing))
    private val pingMutex = Mutex()
    private suspend fun updatePingAtomically(newPing: Ping) {
        pingMutex.withLock {
            ping.value = mostRecent(ping.value, newPing)
        }
    }

    val canContinueOneTime = combine(
        itemListState,
        oneTimeIpAddressState,
        oneTimePasswordState,
        portState,
    ) { itemList, ip, password, port ->
        itemList.isNotEmpty() && ip.isNotEmpty() && password.isNotEmpty() && port.isNotEmpty()
    }

    fun onSaveOneTimeDestinationClicked() {
        onSaveOneTimeDestinationCallback(oneTimeIpAddressState.text, oneTimePasswordState.text) { newDestination ->
            selectedDestinationState.value = newDestination
        }
    }

    fun onAddNewDestinationClicked() {
        onAddNewDestinationCallback { newDestination ->
            selectedDestinationState.value = newDestination
        }
    }

    fun onUserSelectItem() {
        MainActivity.mainActivity.openFilePicker { selectedUri ->
            DocumentFile.fromSingleUri(MainActivity.mainActivity, selectedUri)?.let { docUri ->
                docUri.name?.let { name ->
                    itemListState.value += TransferJob.Item(
                        name = name,
                        uri = docUri.uri,
                        isFile = docUri.isFile,
                        sizeBytes = MainActivity
                            .mainActivity
                            .contentResolver
                            .openAssetFileDescriptor(docUri.uri, "r").use { file ->
                                file?.length?.takeIf { it > 0 } ?: 0
                            },
                    )
                } ?: run { /*TODO*/ }
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
                    ip = oneTimeIpAddressState.text,
                    password = oneTimePasswordState.text,
                ), // TODO
                items = itemListState.value,
                port = portState.text.toInt(),
                status = TransferStatus.Sending,
            ),
        )
    }
}

@Composable
fun SendFilesScreen(
    viewModel: SendFilesScreenViewModel,
    data: SettingsDataAndroid,
    onBackClicked: () -> Unit,
) {
    var destinationPickerActive by remember { mutableStateOf(false) }
    var destinationState by remember { mutableStateOf("Select destination...") }
    var oneTimeDestinationState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = "Send files",
                onBackClicked = onBackClicked,
            )
        },
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FittoniaSpacerHeight(height = 15)

                Text(
                    text = "Select files/folders to send.",
                    style = headingMStyle,
                )
                FittoniaSpacerHeight(height = 5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE)),
                ) {
                    val fileList = viewModel.itemListState.collectAsState()
                    fileList.value.forEachIndexed { index, file ->
                        Row(
                            modifier = Modifier.padding(all = 5.dp),
                            verticalAlignment = CenterVertically,
                        ) {
                            Text(text = file.name)
                            FittoniaSpacerWeightRow()
                            FittoniaIcon(
                                modifier = Modifier
                                    .requiredSize(14.dp)
                                    .clickable {
                                        viewModel.itemListState.value = fileList.value.filter { it != file }
                                    },
                                drawableRes = R.drawable.ic_clear,
                                tint = Color(0xFF222222),
                            )
                        }
                        if (index != fileList.value.lastIndex) {
                            HorizontalLine()
                        }
                    }
                }
                FittoniaSpacerHeight(height = 5)
                Row {
                    FittoniaButton(
                        onClick = viewModel::onUserSelectItem,
                        type = currentStyle.secondaryButtonType,
                        content = {
                            ButtonText(text = "Add")
                            FittoniaSpacerWidth(width = 5)
                            ButtonIcon(drawableRes = R.drawable.ic_add)
                        },
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                }
                FittoniaSpacerHeight(height = 30)
                Text(
                    text = "Destination",
                    style = headingMStyle,
                )
                FittoniaSpacerHeight(height = 10)
                Row(
                    modifier = Modifier.clickable { oneTimeDestinationState = !oneTimeDestinationState },
                    verticalAlignment = CenterVertically,
                ) {
                    Checkbox(
                        checked = oneTimeDestinationState,
                        onCheckedChange = null,
                    )
                    Text(
                        text = "One-time destination",
                        style = psstStyle,
                    )
                }
                FittoniaSpacerHeight(height = 10)
                if (oneTimeDestinationState) {
                    FittoniaSpacerHeight(height = 15)

                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.oneTimeIpAddressState,
                        label = "IP Address/Code",
                    )

                    FittoniaSpacerHeight(height = 15)

                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.oneTimePasswordState,
                        label = "Password",
                    )

                    FittoniaSpacerHeight(height = 10)
                }
                if (data.destinations.isNotEmpty() && !oneTimeDestinationState) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Color(0xFF446644))
                            .background(color = Color(0xFFDDFFEE))
                            .padding(5.dp)
                            .clickable(onClick = { destinationPickerActive = true }),
                    ) {
                        val selectedDestination = viewModel.selectedDestinationState.collectAsState()
                        Row(
                            verticalAlignment = CenterVertically,
                        ) {
                            Text(
                                text = selectedDestination.value?.name ?: "Select destination...",
                                style = readOnlyFieldTextStyle,
                            )
                            FittoniaSpacerWeightRow()
                            FittoniaIcon(
                                modifier = Modifier.requiredSize(10.dp),
                                drawableRes = R.drawable.ic_chevron_down,
                                tint = Color(0xFF222222),
                            )
                        }
                        selectedDestination.value?.let { destination ->
                            HorizontalLine(modifier = Modifier.padding(vertical = 4.dp))
                            listOf(
                                "IP Address: ${destination.ip}",
                                "Password: • • • • • • • • • • • •",
                            ).fastForEach {
                                Text(
                                    modifier = Modifier
                                        .padding(start = 5.dp)
                                        .padding(vertical = 4.dp),
                                    text = it,
                                    style = readOnlyFieldLightTextStyle,
                                )
                            }
                        }
                    }
                }
                FittoniaSpacerHeight(height = 5)
                if (oneTimeDestinationState) {
                    FittoniaButton(
                        type = currentStyle.secondaryButtonType,
                        onClick = viewModel::onSaveOneTimeDestinationClicked,
                    ) {
                        ButtonText(text = "Save as new destination")
                        FittoniaSpacerWidth(width = 5)
                        ButtonIcon(drawableRes = R.drawable.ic_add)
                    }
                } else {
                    FittoniaButton(
                        type = currentStyle.secondaryButtonType,
                        onClick = viewModel::onAddNewDestinationClicked,
                    ) {
                        ButtonText(text = "Add new destination")
                    }
                }

                FittoniaSpacerHeight(height = 10)

                when (viewModel.ping.collectAsState(Ping(PingStatus.NoPing)).value.pingStatus) {
                    is PingStatus.NoPing -> Unit
                    is PingStatus.Processing -> Row {
                        Text(
                            text = stringResource(R.string.send_files_screen_ping_processing),
                            style = paragraphStyle,
                        )
                        FittoniaLoadingIndicator()
                    }

                    is PingStatus.Success -> Text(
                        text = stringResource(R.string.send_files_screen_ping_success),
                        style = paragraphStyle,
                    )

                    is PingStatus.Failure -> Text(
                        text = stringResource(R.string.send_files_screen_ping_failure),
                        style = paragraphStyle,
                    )
                }

                FittoniaSpacerHeight(height = 30)

                FittoniaNumberInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.portState,
                    label = "Port",
                )

                FittoniaSpacerHeight(height = 30)

                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.descriptionState,
                    label = {
                        Row {
                            Text(
                                text = "Description",
                                style = inputLabelStyle,
                            )
                            FittoniaSpacerWidth(width = 4)
                            Text(
                                modifier = Modifier.align(alignment = CenterVertically),
                                text = "(optional)",
                                style = psstStyle,
                            )
                        }
                    },
                )
            }
        },
        footer = {
            FittoniaButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp),
                enabled = if (oneTimeDestinationState) {
                    viewModel.canContinueOneTime.collectAsState(initial = false).value
                } else {
                    viewModel.canContinue.collectAsState(initial = false).value
                },
                content = { ButtonText(text = "Confirm") },
                onClick = viewModel.rememberSuspendedAction(viewModel::onConfirmClicked),
            )
        },
        overlay = {
            FittoniaModal(
                state = destinationPickerActive,
                onDismiss = { destinationPickerActive = false },
            ) {
                listOf(
                    Options(
                        name = "About",
                        onClick = {},
                    ),
                ).fastForEach {
                    data.destinations.forEachIndexed { index, destination ->
                        Row(
                            modifier = Modifier.clickable {
                                destinationState = destination.name
                                viewModel.updateDestination(destination)
                                destinationPickerActive = false
                            },
                        ) {
                            Text(
                                text = destination.name,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                style = paragraphStyle,
                            )
                            FittoniaSpacerWeightRow()
                            FittoniaIcon(
                                modifier = Modifier
                                    .requiredSize(10.dp)
                                    .align(CenterVertically),
                                drawableRes = R.drawable.ic_chevron_right,
                                tint = Color(0xFF222222),
                            )
                        }

                        if (index != data.destinations.lastIndex) {
                            HorizontalLine()
                        }
                    }
                }
            }
        },
    )
}

@Composable
@Preview
private fun Preview() {
    SendFilesScreen(
        viewModel = SendFilesScreenViewModel(
            onSaveOneTimeDestinationCallback = { _, _, _ -> },
            onAddNewDestinationCallback = { _ -> },
            onPing = { _, _, _, _ -> Ping(PingStatus.NoPing) },
            onConfirmCallback = { },
        ),
        data = SettingsDataAndroid(
            destinations = persistentListOf(
                SettingsManager.Destination(
                    name = "Home Computer",
                    ip = "192.456.34.01",
                    password = "Password",
                ),
                SettingsManager.Destination(
                    name = "Work Computer",
                    ip = "193.852.11.02",
                    password = "Password",
                ),
                SettingsManager.Destination(
                    name = "Bob's Computer",
                    ip = "164.123.45.67",
                    password = "Password",
                ),
            ),
        ),
        onBackClicked = { },
    )
}
