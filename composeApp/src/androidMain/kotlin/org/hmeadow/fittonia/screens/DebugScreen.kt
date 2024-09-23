package org.hmeadow.fittonia.screens

import SettingsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.AndroidServer
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.FittoniaTextInput
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.InputFlow
import org.hmeadow.fittonia.design.fonts.headingSStyle
import org.hmeadow.fittonia.utility.rememberSuspendedAction

class DebugScreenViewModel : BaseViewModel() {
    val deviceIp = MutableStateFlow("Unknown")

    val newDumpDirectory = InputFlow(initial = "Job${(Math.random() * 10000).toInt()}")

    init {
        refreshIp()
    }

    fun refreshIp() {
        deviceIp.value = MainActivity.mainActivity.getDeviceIpAddress() ?: "Unknown"
    }

    fun createDumpDirectory() {
        launch { MainActivity.mainActivity.createDumpDirectory(newDumpDirectory.value, ::println) }
    }
}

@Composable
fun DebugScreen(
    viewModel: DebugScreenViewModel,
    data: SettingsDataAndroid,
    onResetSettingsClicked: () -> Unit,
    onClearDumpPath: () -> Unit,
    onRemoveDestinationClicked: (SettingsManager.Destination) -> Unit,
    onBackClicked: () -> Unit,
    debugNewThread: suspend () -> Unit,
    debugNewDestination: () -> Unit,
) {
    var debugAlertsState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = "Debug Screen",
                onBackClicked = onBackClicked,
            )
        },
        content = {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Text(
                    text = "MainActivity/MainViewModel",
                    style = headingSStyle,
                )
                Row {
                    Column {
                        Text(text = "Current device IP:")
                        Text(text = "Default Server Port:")
                        Text(text = "Temporary Server Port:")
                        Text(text = "Server Password:")
                        Text(text = "Dump Path:")
                    }
                    HMSpacerWidth(width = 10)
                    Column {
                        Row {
                            Text(text = viewModel.deviceIp.collectAsState().value)
                            HMSpacerWidth(width = 10)
                            Text(
                                modifier = Modifier.clickable(onClick = viewModel::refreshIp),
                                text = "Refresh",
                                color = Color.Cyan,
                            )
                        }
                        Text(text = data.defaultPort.toString())
                        Text(text = data.temporaryPort.toString())
                        Text(text = data.serverPassword.toString())
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .background(Color.LightGray),
                        ) {
                            Text(text = data.dumpPath.dumpUriPath)

                            HMSpacerWeightRow()

                            FittoniaIcon(
                                modifier = Modifier
                                    .requiredSize(20.dp)
                                    .clickable(onClick = onClearDumpPath),
                                drawableRes = R.drawable.ic_clear,
                            )
                        }
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .background(Color.LightGray),
                        ) {
                            Text(text = data.dumpPath.dumpPathReadable)
                        }
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .background(Color.LightGray),
                        ) {
                            Text(text = data.dumpPath.dumpPathForReal)
                        }
                    }
                }
                Text(
                    text = "Active AndroidServer",
                    style = headingSStyle,
                )
                AndroidServer.server.value?.let { server ->
                    Row {
                        Column {
                            Text(text = "Socket:")
                            Text(text = "Socket Job:")
                            Text(text = ".server.value:")
                        }
                        HMSpacerWidth(width = 10)
                        Column {
                            Text(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                text = server.serverSocket.toString(),
                            )
                            Text(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                text = server.serverJob.toString(),
                            )
                            Text(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                text = AndroidServer.server.collectAsState().value.toString(),
                            )
                        }
                    }
                } ?: Text(text = "OFFLINE")

                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { debugAlertsState = true },
                ) {
                    ButtonText(text = "Alerts")
                }
                FittoniaTextInput(
                    inputFlow = viewModel.newDumpDirectory,
                    label = "New Dump Directory",
                )
                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::createDumpDirectory,
                ) {
                    ButtonText(text = "Create dump directory")
                }
                Text(
                    text = "Destinations",
                    style = headingSStyle,
                )
                data.destinations.forEach { destination ->
                    Row(
                        modifier = Modifier.background(color = Color.LightGray),
                        verticalAlignment = CenterVertically,
                    ) {
                        Column {
                            Text(text = "Name: ${destination.name}")
                            Text(text = "IP: ${destination.ip}")
                            Text(text = "Password: ${destination.password}")
                        }

                        HMSpacerWeightRow()

                        FittoniaIcon(
                            modifier = Modifier
                                .requiredSize(20.dp)
                                .clickable { onRemoveDestinationClicked(destination) },
                            drawableRes = R.drawable.ic_clear,
                        )
                    }
                    HMSpacerHeight(height = 10)
                }
            }
        },
        footer = {
            Footer {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FittoniaButton(onClick = viewModel.rememberSuspendedAction(debugNewThread)) {
                            ButtonText(text = "New Thread")
                        }
                        HMSpacerWeightRow()
                        FittoniaButton(onClick = debugNewDestination) {
                            ButtonText(text = "New Destination")
                        }
                    }
                    FittoniaButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onResetSettingsClicked,
                    ) {
                        ButtonText(text = "Reset Settings")
                    }
                }
            }
        },
        overlay = {
            FittoniaModal(
                state = debugAlertsState,
                onDismiss = { debugAlertsState = false },
            ) { _ ->
                Column {
                    FittoniaButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            MainActivity.mainActivityForServer?.alert(UserAlert.PortInUse(port = 42069))
                        },
                    ) {
                        ButtonIcon(drawableRes = R.drawable.ic_add)
                        HMSpacerWidth(width = 5)
                        ButtonText(text = "UserAlert.PortInUse")
                    }
                }
            }
        },
    )
}
