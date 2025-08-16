package org.hmeadow.fittonia.screens.debugScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.androidServer.AndroidServer
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingSStyle
import org.hmeadow.fittonia.utility.DebugTurnOff.allowDebug
import org.hmeadow.fittonia.utility.encodeIpAddress
import org.hmeadow.fittonia.utility.isLandscape
import org.hmeadow.fittonia.utility.isXLARGE

@Composable
fun DebugScreenOverviewTab(
    modifier: Modifier = Modifier,
    viewModel: DebugScreenViewModel,
    footerHeight: Dp,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Overview",
            style = headingLStyle,
        )
        FittoniaSpacerHeight(height = 10)
        Row {
            Column {
                Text(text = "Current device IP:")
                Text(text = "Current device code:")
                Text(text = "Orientation:")
                Text(text = "Device Size:")
            }
            FittoniaSpacerWidth(width = 10)
            Column {
                Row {
                    Text(text = viewModel.deviceIp.collectAsState().value)
                    FittoniaSpacerWidth(width = 10)
                    Text(
                        modifier = Modifier.clickable(onClick = viewModel::refreshIp),
                        text = "Refresh",
                        color = Color.Cyan,
                    )
                }
                Row {
                    Text(text = encodeIpAddress(ipAddress = viewModel.deviceIp.collectAsState().value))
                    FittoniaSpacerWidth(width = 10)
                    Text(
                        modifier = Modifier.clickable(onClick = viewModel::refreshIp),
                        text = "Refresh",
                        color = Color.Cyan,
                    )
                }
                Text(
                    text = if (isLandscape()) {
                        "Landscape"
                    } else {
                        "Portrait"
                    },
                )
                Text(
                    text = if (isXLARGE()) "Tablet" else "Phone",
                )
            }
        }
        FittoniaSpacerHeight(height = 10)
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
                FittoniaSpacerWidth(width = 10)
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
        } ?: run {
            if (MainActivity.mainActivity.hasBoundServer) {
                Text(text = "OFFLINE (Check AndroidManifest.xml for errors.)")
            } else {
                Text(text = "OFFLINE")
            }
            FittoniaButton(
                onClick = {
                    AndroidServer.socketLogDebug = true
                    MainActivity.mainActivity.attemptStartServer()
                },
            ) {
                ButtonText(text = "Restart Server")
            }
        }

        FittoniaButton(onClick = { allowDebug = false }) {
            ButtonText(text = "Turn off debug")
        }

        FittoniaSpacerHeight(footerHeight)
    }
}
