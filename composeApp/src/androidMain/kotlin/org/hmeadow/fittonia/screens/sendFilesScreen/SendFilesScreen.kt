package org.hmeadow.fittonia.screens.sendFilesScreen

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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.update
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.EquivalentIpCodeText
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaCheckbox
import org.hmeadow.fittonia.compose.components.FittoniaLoadingIndicator
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing2
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingMStyle
import org.hmeadow.fittonia.design.fonts.inputLabelStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.design.fonts.psstStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldLightTextStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldTextStyle
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.screens.overviewScreen.Options
import org.hmeadow.fittonia.utility.InfoBorderState.InfoBoxOverlay
import org.hmeadow.fittonia.utility.rememberSuspendedAction

@Composable
internal fun SendFilesScreen(
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
                FittoniaSpacerHeight(height = spacing4)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE)),
                ) {
                    val fileList = viewModel.itemListState.collectAsState()
                    fileList.value.forEachIndexed { index, file ->
                        Row(
                            modifier = Modifier.padding(all = spacing4),
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
                FittoniaSpacerHeight(height = spacing4)
                Row {
                    FittoniaButton(
                        onClick = viewModel::onUserSelectItem,
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
                    Spacer(modifier = Modifier.weight(1.0f))
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

                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.oneTimeIpAddressState,
                        label = stringResource(R.string.send_files_screen_ip_address_code),
                        // todo hint = "Tip: Check destination's \"This Device\" tab",
                    )

                    FittoniaSpacerHeight(height = spacing4)

                    EquivalentIpCodeText(equivalentIPCode = viewModel.equivelentIpOrCode.collectAsState().value)

                    FittoniaSpacerHeight(height = spacing16)

                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.oneTimeAccessCodeState,
                        label = stringResource(R.string.send_files_screen_access_code), // TODO trim whitespace.
                    )

                    FittoniaSpacerHeight(height = spacing32)
                }
                if (data.destinations.isNotEmpty() && !oneTimeDestinationState) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = currentStyle.primaryButtonType.borderColour)
                            .background(color = currentStyle.primaryButtonType.backgroundColor)
                            .padding(all = spacing4)
                            .clickable(onClick = { destinationPickerActive = true }),
                    ) {
                        val selectedDestination = viewModel.selectedDestinationState.collectAsState()
                        Row(
                            modifier = Modifier.padding(start = spacing4),
                            verticalAlignment = CenterVertically,
                        ) {
                            Text(
                                text = selectedDestination.value?.name
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
                        selectedDestination.value?.let { destination ->
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
                FittoniaSpacerHeight(height = spacing4)
                if (oneTimeDestinationState) {
                    FittoniaButton(
                        type = currentStyle.secondaryButtonType,
                        onClick = viewModel::onSaveOneTimeDestinationClicked,
                    ) {
                        ButtonText(text = stringResource(R.string.send_files_screen_save_new_destination))
                        FittoniaSpacerWidth(width = spacing4)
                        ButtonIcon(drawableRes = R.drawable.ic_add)
                    }
                } else {
                    FittoniaButton(
                        onClick = viewModel::onAddNewDestinationClicked,
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

                FittoniaSpacerHeight(height = spacing8)

                // TODO move the ping status somewhere you can see it as you type ip and code.
                when (viewModel.ping.collectAsState(Ping(PingStatus.NoPing)).value.pingStatus) {
                    is PingStatus.NoPing -> Unit
                    is PingStatus.Processing -> Row {
                        Text(
                            text = stringResource(R.string.send_files_screen_ping_processing),
                            style = paragraphTextStyle,
                        )
                        FittoniaLoadingIndicator()
                    }

                    is PingStatus.Success -> Text(
                        text = stringResource(R.string.send_files_screen_ping_success),
                        style = paragraphTextStyle,
                    )

                    is PingStatus.Failure -> Text(
                        text = stringResource(R.string.send_files_screen_ping_failure),
                        style = paragraphTextStyle,
                    )
                }

                FittoniaSpacerHeight(height = spacing32)

                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.descriptionState,
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
            FittoniaModal(
                state = destinationPickerActive,
                onDismiss = { destinationPickerActive = false },
            ) {
                listOf(
                    Options(
                        name = "About", // TODO what is this about?
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
    )
}
