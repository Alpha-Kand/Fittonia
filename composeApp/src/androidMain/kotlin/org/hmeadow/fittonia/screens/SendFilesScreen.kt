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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.documentfile.provider.DocumentFile
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaButtonType
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaNumberInput
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.FittoniaTextInput
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.InputFlow
import org.hmeadow.fittonia.components.headingMStyle
import org.hmeadow.fittonia.components.inputLabelStyle
import org.hmeadow.fittonia.components.paragraphStyle
import org.hmeadow.fittonia.components.psstStyle
import org.hmeadow.fittonia.components.readOnlyLightStyle
import org.hmeadow.fittonia.components.readOnlyStyle
import kotlin.random.Random

class SendFilesScreenViewModel(
    private val onSaveOneTimeDestinationCallback: (
        ip: String,
        password: String,
        onFinish: (newDestination: SettingsManager.Destination) -> Unit,
    ) -> Unit,
    private val onAddNewDestinationCallback: (onFinish: (newDestination: SettingsManager.Destination) -> Unit) -> Unit,
    private val onConfirmCallback: (TransferJob) -> Unit,
) : BaseViewModel {
    val itemListState = MutableStateFlow<List<TransferJob.Item>>(emptyList())
    val selectedDestinationState = MutableStateFlow<SettingsManager.Destination?>(null)
    val portState = InputFlow(initial = "")
    val descriptionState = InputFlow(initial = "")

    val oneTimeIpAddressState = InputFlow(initial = "")
    val oneTimePasswordState = InputFlow(initial = "")

    val canContinue = combine(
        itemListState,
        selectedDestinationState,
        portState,
    ) { itemList, _, port ->
        itemList.isNotEmpty() && port.isNotEmpty()
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
        onSaveOneTimeDestinationCallback(oneTimeIpAddressState.value, oneTimePasswordState.value) { newDestination ->
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
                    )
                } ?: run { /*TODO*/ }
            }
        }
    }

    fun onConfirmClicked() {
        onConfirmCallback(
            TransferJob(
                id = -1,
                description = descriptionState.value.takeIf { it.isNotEmpty() } ?: "Job ${Random.nextInt()}", // TODO
                destination = selectedDestinationState.value ?: SettingsManager.Destination(
                    name = "-",
                    ip = oneTimeIpAddressState.value,
                    password = oneTimePasswordState.value,
                ), // TODO
                items = itemListState.value,
                port = portState.value.toInt(),
                status = TransferStatus.Sending,
                direction = TransferJob.Direction.OUTGOING,
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
                HMSpacerHeight(height = 15)

                Text(
                    text = "Select files/folders to send.",
                    style = headingMStyle,
                )
                HMSpacerHeight(height = 5)
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
                            HMSpacerWeightRow()
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
                HMSpacerHeight(height = 5)
                Row {
                    FittoniaButton(
                        onClick = viewModel::onUserSelectItem,
                        type = FittoniaButtonType.Secondary,
                        content = {
                            ButtonText(text = "Add")
                            HMSpacerWidth(width = 5)
                            ButtonIcon(drawableRes = R.drawable.ic_add)
                        },
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                }
                HMSpacerHeight(height = 30)
                Text(
                    text = "Destination",
                    style = headingMStyle,
                )
                HMSpacerHeight(height = 10)
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
                HMSpacerHeight(height = 10)
                if (oneTimeDestinationState) {
                    HMSpacerHeight(height = 15)
                    Text(
                        text = "IP Address/Code",
                        style = inputLabelStyle,
                    )

                    HMSpacerHeight(height = 5)

                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.oneTimeIpAddressState,
                    )

                    HMSpacerHeight(height = 15)

                    Text(
                        text = "Password",
                        style = inputLabelStyle,
                    )

                    HMSpacerHeight(height = 5)

                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.oneTimePasswordState,
                    )

                    HMSpacerHeight(height = 10)
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
                        val foo = viewModel.selectedDestinationState.collectAsState()
                        Row(
                            verticalAlignment = CenterVertically,
                        ) {
                            Text(
                                text = foo.value?.name ?: "Select destination...",
                                style = readOnlyStyle,
                            )
                            HMSpacerWeightRow()
                            FittoniaIcon(
                                modifier = Modifier.requiredSize(10.dp),
                                drawableRes = R.drawable.ic_chevron_down,
                                tint = Color(0xFF222222),
                            )
                        }
                        foo.value?.let { destination ->
                            HorizontalLine(modifier = Modifier.padding(vertical = 4.dp))
                            listOf(
                                "IP Address: ${destination.ip}",
                                "Password: ••••••••••••",
                            ).fastForEach {
                                Text(
                                    modifier = Modifier
                                        .padding(start = 5.dp)
                                        .padding(vertical = 4.dp),
                                    text = it,
                                    style = readOnlyLightStyle,
                                )
                            }
                        }
                    }
                }
                HMSpacerHeight(height = 5)
                if (oneTimeDestinationState) {
                    FittoniaButton(
                        type = FittoniaButtonType.Secondary,
                        onClick = viewModel::onSaveOneTimeDestinationClicked,
                    ) {
                        ButtonText(text = "Save as new destination")
                        HMSpacerWidth(width = 5)
                        ButtonIcon(drawableRes = R.drawable.ic_add)
                    }
                } else {
                    FittoniaButton(
                        type = FittoniaButtonType.Secondary,
                        onClick = viewModel::onAddNewDestinationClicked,
                    ) {
                        ButtonText(text = "Add new destination")
                    }
                }

                HMSpacerHeight(height = 30)

                Text(
                    text = "Port",
                    style = inputLabelStyle,
                )

                HMSpacerHeight(height = 5)

                FittoniaNumberInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.portState,
                )

                HMSpacerHeight(height = 30)
                Row {
                    Text(
                        text = "Description",
                        style = inputLabelStyle,
                    )
                    HMSpacerWidth(width = 4)
                    Text(
                        modifier = Modifier.align(alignment = CenterVertically),
                        text = "(optional)",
                        style = psstStyle,
                    )
                }
                HMSpacerHeight(height = 5)
                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.descriptionState,
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
                onClick = viewModel::onConfirmClicked,
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
                                viewModel.selectedDestinationState.value = destination
                                destinationPickerActive = false
                            },
                        ) {
                            Text(
                                text = destination.name,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                style = paragraphStyle,
                            )
                            HMSpacerWeightRow()
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
