package org.hmeadow.fittonia.screens.alertsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaLinkText
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaNumberInput
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.description
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingSStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.mainActivity.MainActivity
import org.hmeadow.fittonia.title
import org.hmeadow.fittonia.utility.rememberSuspendedAction
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun AlertsScreen(
    viewModel: AlertsScreenViewModel,
    onBackClicked: () -> Unit,
) {
    var temporaryPortModalState by remember { mutableStateOf(false) }
    var newDefaultPortModalState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = "Notifications",
                onBackClicked = onBackClicked,
            )
        },
        content = {
            Column {
                UserAlert.userAlerts.collectAsState().value.fastForEach { alert ->
                    when (alert) {
                        is UserAlert.PortInUse -> PortInUseTile(
                            alert = alert,
                            onTemporaryPortClicked = { temporaryPortModalState = true },
                            onNewDefaultPortClicked = { newDefaultPortModalState = true },
                        )

                        is UserAlert.DumpLocationLost -> DumpLocationLostTile(
                            onPickLocationClicked = {
                                MainActivity.mainActivity.openFolderPicker(viewModel::onDumpPathPicked)
                            },
                        )
                    }
                }
            }
        },
        overlay = {
            FittoniaModal(
                state = temporaryPortModalState,
                onDismiss = { temporaryPortModalState = false },
            ) { _ ->
                PortInputDialog(
                    label = "Temporary port",
                    inputFlow = viewModel.temporaryPort,
                    onAccept = viewModel.rememberSuspendedAction {
                        temporaryPortModalState = false
                        viewModel.onTemporaryPortAccepted()
                    },
                )
            }

            FittoniaModal(
                state = newDefaultPortModalState,
                onDismiss = { newDefaultPortModalState = false },
            ) { _ ->
                PortInputDialog(
                    label = "New default port",
                    inputFlow = viewModel.newDefaultPort,
                    onAccept = viewModel.rememberSuspendedAction {
                        newDefaultPortModalState = false
                        viewModel.onNewDefaultPortAccepted()
                    },
                )
            }
        },
    )
}

@Composable
private fun PortInputDialog(
    label: String,
    inputFlow: InputFlow,
    onAccept: () -> Unit,
) {
    Column(modifier = Modifier.padding(all = spacing16)) {
        FittoniaNumberInput(
            label = label,
            modifier = Modifier.fillMaxWidth(),
            inputFlow = inputFlow,
        )
        FittoniaSpacerHeight(height = spacing8)
        Row {
            FittoniaSpacerWeightRow()
            FittoniaButton(onClick = onAccept) {
                ButtonText(text = "Accept")
            }
        }
    }
}

@Composable
private fun PortInUseTile(
    alert: UserAlert.PortInUse,
    onTemporaryPortClicked: () -> Unit,
    onNewDefaultPortClicked: () -> Unit,
) {
    AlertTile(
        title = alert.title(),
        description = alert.description(),
        actions = listOf(
            "New temporary port" to onTemporaryPortClicked,
            "New default port" to onNewDefaultPortClicked,
        ),
    )
}

@Composable
private fun DumpLocationLostTile(
    onPickLocationClicked: () -> Unit,
) {
    AlertTile(
        title = UserAlert.DumpLocationLost.title(),
        description = UserAlert.DumpLocationLost.description(),
        actions = listOf("Select new location" to onPickLocationClicked),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlertTile(
    title: String,
    description: String,
    actions: List<Pair<String, () -> Unit>> = emptyList(),
) {
    var expandedState by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .background(color = Color(0xFFFFDFDA))
            .padding(all = 15.dp),
    ) {
        Column(
            modifier = Modifier.weight(1.0f),
        ) {
            Row(
                modifier = Modifier.clickable { expandedState = !expandedState },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = headingSStyle,
                )
                FittoniaSpacerWeightRow()
                FittoniaIcon(
                    modifier = Modifier
                        .requiredHeight(16.dp)
                        .padding(horizontal = 7.dp),
                    drawableRes = if (expandedState) {
                        R.drawable.ic_chevron_up
                    } else {
                        R.drawable.ic_chevron_down
                    },
                )
            }
            if (expandedState) {
                FittoniaSpacerHeight(height = 7)
                Text(
                    text = description,
                    style = paragraphTextStyle,
                )
                FittoniaSpacerHeight(height = 8)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    actions.fastForEach {
                        FittoniaLinkText(
                            modifier = Modifier.padding(all = 4.dp),
                            text = it.first,
                            onClick = it.second,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    AlertsScreen(
        viewModel = AlertsScreenViewModel(
            onUpdateDumpPath = {},
            onTemporaryPortAcceptedCallback = {},
            onNewDefaultPortAcceptedCallback = {},
        ),
        onBackClicked = {},
    )
}
