package org.hmeadow.fittonia.screens.sendFilesScreen

import SettingsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.update
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.EquivalentIPCode
import org.hmeadow.fittonia.components.EquivalentIpCodeText
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.PingStatusComponent
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaCheckbox
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing2
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.emoticonStyle
import org.hmeadow.fittonia.design.fonts.headingMStyle
import org.hmeadow.fittonia.design.fonts.inputLabelStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.design.fonts.psstStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldLightTextStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldTextStyle
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.utility.ContinueStatusIcon
import org.hmeadow.fittonia.utility.InfoBorderState.InfoBoxOverlay
import org.hmeadow.fittonia.utility.pingStatus
import org.hmeadow.fittonia.utility.rememberSuspendedAction

@Composable
internal fun SendFilesScreen(
    viewModel: SendFilesScreenViewModel,
    data: SettingsDataAndroid,
    onBackClicked: () -> Unit,
    onDeleteDestinations: (List<String>) -> Unit,
) {
    var destinationPickerActive by remember { mutableStateOf(false) }
    var destinationState by remember { mutableStateOf("Select destination...") }
    var oneTimeDestinationState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = stringResource(R.string.send_files_screen_heading),
                includeInfoButton = true,
                onBackClicked = onBackClicked,
            )
        },
        content = {
            Column(modifier = Modifier.padding(horizontal = spacing16)) {
                FittoniaSpacerHeight(height = spacing16)

                Text(
                    text = stringResource(R.string.send_files_screen_select_files_folders),
                    style = headingMStyle,
                )

                FittoniaSpacerHeight(height = spacing8)

                Row(verticalAlignment = Top) {
                    val itemList = viewModel.itemListState.collectAsState().value

                    ContinueStatusIcon(
                        modifier = Modifier.padding(top = if (itemList.isEmpty()) spacing4 else 0.dp),
                        continueStatus = viewModel.itemListContinue.collect(),
                    )

                    FittoniaSpacerWidth(width = spacing16)

                    Column {
                        SelectedFileList(
                            selectedFiles = itemList,
                            updatedSelectedFiles = { viewModel.itemListState.value = it },
                        )

                        AddFileButton(onClicked = viewModel::onUserSelectItem)
                    }

                    FittoniaSpacerWeightRow()
                }

                FittoniaSpacerHeight(height = spacing32)
                Text(
                    text = stringResource(R.string.send_files_screen_add_destination),
                    style = headingMStyle,
                )
                FittoniaSpacerHeight(height = spacing8)
                FittoniaCheckbox(label = stringResource(R.string.send_files_screen_one_time_destination)) { state ->
                    oneTimeDestinationState = state
                    if (!state) {
                        viewModel.ping.update { Ping(PingStatus.NoPing) }
                    }
                }

                FittoniaSpacerHeight(height = spacing8)

                if (oneTimeDestinationState) {
                    FittoniaSpacerHeight(height = spacing16)

                    Row(verticalAlignment = Alignment.Bottom) {
                        val equivelentIpOrCode = viewModel.equivelentIpOrCode.collectAsState().value
                        val continueStatusBottomPadding = if (equivelentIpOrCode !is EquivalentIPCode.Neither) {
                            23.dp
                        } else {
                            0.dp
                        }

                        ContinueStatusIcon(
                            modifier = Modifier.padding(bottom = continueStatusBottomPadding),
                            continueStatus = viewModel.oneTimeIpAddressContinue.collect(),
                        )

                        FittoniaSpacerWidth(width = spacing16)

                        Column {
                            FittoniaTextInput(
                                modifier = Modifier.fillMaxWidth(),
                                inputFlow = viewModel.oneTimeIpAddressState,
                                label = stringResource(R.string.send_files_screen_ip_address_code),
                                // todo hint = "Tip: Check destination's \"This Device\" tab",
                            )

                            FittoniaSpacerHeight(height = spacing4)

                            EquivalentIpCodeText(equivalentIPCode = viewModel.equivelentIpOrCode.collectAsState().value)
                        }
                    }

                    FittoniaSpacerHeight(height = spacing16)

                    Row(verticalAlignment = Alignment.Bottom) {
                        ContinueStatusIcon(continueStatus = viewModel.oneTimeAccessCodeContinue.collect())

                        FittoniaSpacerWidth(width = spacing16)

                        FittoniaTextInput(
                            modifier = Modifier.fillMaxWidth(),
                            inputFlow = viewModel.oneTimeAccessCodeState,
                            label = stringResource(R.string.send_files_screen_access_code), // TODO trim whitespace.
                        )
                    }
                    FittoniaSpacerHeight(height = spacing32)
                }

                if (data.destinations.isNotEmpty() && !oneTimeDestinationState) {
                    Row {
                        ContinueStatusIcon(continueStatus = viewModel.selectedDestinationContinue.collect())
                        FittoniaSpacerWidth(width = spacing16)
                        SelectDestinationPicker(
                            onClicked = { destinationPickerActive = true },
                            selectedDestination = viewModel.selectedDestinationState.collectAsState().value,
                        )
                    }
                }

                FittoniaSpacerHeight(height = spacing4)

                if (oneTimeDestinationState) {
                    SaveNewDestination(onClicked = viewModel::onSaveOneTimeDestinationClicked)
                } else {
                    AddNewDestinationButton(onClicked = viewModel::onAddNewDestinationClicked)
                }

                FittoniaSpacerHeight(height = spacing8)

                PingStatusComponent(pingStatus = viewModel.pingStatus)

                FittoniaSpacerHeight(height = spacing32)

                DescriptionInputField(inputFlow = viewModel.descriptionState)

                FittoniaSpacerHeight(height = spacing32)
            }
        },
        footer = {
            FittoniaButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = spacing8),
                enabled = if (oneTimeDestinationState) {
                    viewModel.canContinueOneTime.collectAsState(initial = false).value
                } else {
                    viewModel.canContinue.collectAsState(initial = false).value
                },
                content = { ButtonText(text = stringResource(R.string.send_files_screen_confirm)) },
                onClick = viewModel.rememberSuspendedAction(viewModel::onConfirmClicked),
            )
        },
        overlay = {
            InfoBoxOverlay()
            var deleteDestinations by remember(data.destinations) {
                mutableStateOf(List(data.destinations.size) { false })
            }
            FittoniaModal(
                state = destinationPickerActive,
                onDismiss = {
                    destinationPickerActive = false
                    deleteDestinations = List(data.destinations.size) { false }
                },
                topContent = {
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.End)
                            .alpha(if (deleteDestinations.find { true } == true) 1.0f else 0f)
                            .padding(top = spacing8, bottom = spacing16)
                            .clickable {
                                onDeleteDestinations(
                                    deleteDestinations.mapIndexed { index, bool ->
                                        data.destinations[index].name.takeIf { bool }
                                    }.filterNotNull(),
                                )
                            },
                        text = stringResource(R.string.trash_emoticon),
                        style = emoticonStyle,
                    )
                },
            ) {
                data.destinations.forEachIndexed { index, destination ->
                    Row(
                        modifier = Modifier.combinedClickable(
                            onLongClick = {
                                deleteDestinations = deleteDestinations
                                    .toMutableList()
                                    .apply {
                                        this[index] = !this[index]
                                    }
                            },
                            onClick = {
                                destinationState = destination.name
                                viewModel.updateDestination(destination)
                                destinationPickerActive = false
                            },
                        ),
                    ) {
                        if (deleteDestinations[index]) {
                            Text(
                                text = stringResource(R.string.failure_emoticon),
                                modifier = Modifier.padding(horizontal = spacing8, vertical = spacing8),
                                style = paragraphTextStyle,
                            )
                        }

                        Text(
                            text = destination.name,
                            modifier = Modifier
                                .padding(horizontal = spacing8, vertical = spacing8),
                            style = paragraphTextStyle,
                        )
                        FittoniaSpacerWeightRow()
                        FittoniaIcon(
                            modifier = Modifier
                                .requiredSize(size = spacing8)
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
        },
    )
}

@Composable
private fun SelectDestinationPicker(
    onClicked: () -> Unit,
    selectedDestination: SettingsManager.Destination?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = currentStyle.primaryButtonType.borderColour)
            .background(color = currentStyle.primaryButtonType.backgroundColor)
            .padding(all = spacing4)
            .clickable(onClick = onClicked),
    ) {
        Row(
            modifier = Modifier.padding(start = spacing4),
            verticalAlignment = CenterVertically,
        ) {
            Text(
                text = selectedDestination?.name
                    ?: stringResource(R.string.send_files_screen_select_destination),
                style = readOnlyFieldTextStyle,
                color = currentStyle.primaryButtonType.contentColour,
            )
            FittoniaSpacerWeightRow()
            FittoniaIcon(
                modifier = Modifier.requiredSize(10.dp),
                drawableRes = R.drawable.ic_chevron_down,
                tint = currentStyle.primaryButtonType.contentColour,
            )
        }
        selectedDestination?.let { destination ->
            FittoniaSpacerHeight(height = spacing4)
            HorizontalLine()
            FittoniaSpacerHeight(height = spacing8)
            listOf(
                // TODO fix alignment here.
                "IP Address: ${destination.ip}",
                "Access Code: • • • • • • • • • • • •",
            ).fastForEach {
                Text(
                    modifier = Modifier
                        .padding(start = spacing4)
                        .padding(vertical = spacing2),
                    text = it,
                    style = readOnlyFieldLightTextStyle,
                )
            }
        }
    }
}

@Composable
private fun SelectedFileList(
    selectedFiles: List<TransferJob.Item>,
    updatedSelectedFiles: (List<TransferJob.Item>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = Color(0xFF446644))
            .background(color = Color(0xFFDDFFEE)),
    ) {
        selectedFiles.forEachIndexed { index, file ->
            Row(
                modifier = Modifier.padding(all = spacing4),
                verticalAlignment = CenterVertically,
            ) {
                Text(text = file.name)
                FittoniaSpacerWeightRow()
                FittoniaIcon(
                    modifier = Modifier
                        .requiredSize(spacing16)
                        .clickable { updatedSelectedFiles(selectedFiles.filter { it != file }) },
                    drawableRes = R.drawable.ic_clear,
                    tint = Color(0xFF222222),
                )
            }
            if (index != selectedFiles.lastIndex) {
                HorizontalLine()
            }
        }
    }
}

@Composable
private fun AddFileButton(modifier: Modifier = Modifier, onClicked: () -> Unit) {
    FittoniaButton(
        modifier = modifier,
        onClick = onClicked,
        content = {
            ButtonText(text = stringResource(R.string.send_files_screen_add_file))
            FittoniaSpacerWidth(width = spacing4)
            ButtonIcon(drawableRes = R.drawable.ic_add)
        },
        onInfo = {
            Column {
                Text(
                    text = stringResource(R.string.send_files_screen_add_item_button_info1),
                    style = paragraphTextStyle,
                )
                FittoniaSpacerHeight(height = spacing8)
                Text(
                    text = stringResource(R.string.send_files_screen_add_item_button_info2),
                    style = paragraphTextStyle,
                )
            }
        },
    )
}

@Composable
private fun DescriptionInputField(inputFlow: InputFlow) {
    FittoniaTextInput(
        modifier = Modifier.fillMaxWidth(),
        inputFlow = inputFlow,
        label = {
            Row {
                Text(
                    text = stringResource(R.string.send_files_screen_description),
                    style = inputLabelStyle,
                )
                FittoniaSpacerWidth(width = spacing4)
                Text(
                    modifier = Modifier.align(alignment = CenterVertically),
                    text = stringResource(R.string.send_files_screen_description_optional),
                    style = psstStyle,
                )
            }
        },
        onInfo = {
            Text(
                text = stringResource(R.string.send_files_screen_description_field_info),
                style = paragraphTextStyle,
            )
        },
    )
}

@Composable
private fun SaveNewDestination(onClicked: () -> Unit) {
    FittoniaButton(
        type = currentStyle.secondaryButtonType,
        onClick = onClicked,
    ) {
        ButtonText(text = stringResource(R.string.send_files_screen_save_new_destination))
        FittoniaSpacerWidth(width = spacing4)
        ButtonIcon(drawableRes = R.drawable.ic_add)
    }
}

@Composable
private fun AddNewDestinationButton(onClicked: () -> Unit) {
    FittoniaButton(
        onClick = onClicked,
        onInfo = {
            Column {
                Text(
                    text = stringResource(R.string.send_files_screen_add_destination_button_info1),
                    style = paragraphTextStyle,
                )
                FittoniaSpacerHeight(height = spacing8)
                Text(
                    text = stringResource(R.string.send_files_screen_add_destination_button_info2),
                    style = paragraphTextStyle,
                )
            }
        },
    ) {
        ButtonText(text = stringResource(R.string.send_files_screen_add_destination))
    }
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
                    accessCode = "AccessCode",
                ),
                SettingsManager.Destination(
                    name = "Work Computer",
                    ip = "193.852.11.02",
                    accessCode = "AccessCode",
                ),
                SettingsManager.Destination(
                    name = "Bob's Computer",
                    ip = "164.123.45.67",
                    accessCode = "AccessCode",
                ),
            ),
        ),
        onBackClicked = { },
        onDeleteDestinations = {},
    )
}
