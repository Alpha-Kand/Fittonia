package org.hmeadow.fittonia.screens.debugScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.AndroidServer
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingSStyle
import org.hmeadow.fittonia.utility.isLandscape
import org.hmeadow.fittonia.utility.isXLARGE

@Composable
fun DebugScreenOverviewTab(
    modifier: Modifier = Modifier,
    viewModel: DebugScreenViewModel,
    data: SettingsDataAndroid,
    footerHeight: Dp,
    onClearDumpPath: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp),
    ) {
        Text(
            text = "Overview",
            style = headingLStyle,
        )
        HMSpacerHeight(height = 10)
        Row {
            Column {
                Text(text = "Current device IP:")
                Text(text = "Default Server Port:")
                Text(text = "Temporary Server Port:")
                Text(text = "Server Password:")
                Text(text = "Dump 'URI' Path:")
                Text(text = "Dump 'Readable' Path:")
                Text(text = "Dump 'ForReal' Path:")
                Text(text = "Orientation:")
                Text(text = "Device Size:")
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
        HMSpacerHeight(height = 10)
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
        HMSpacerHeight(footerHeight)
    }
}
