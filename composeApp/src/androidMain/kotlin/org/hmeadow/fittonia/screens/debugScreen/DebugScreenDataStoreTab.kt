package org.hmeadow.fittonia.screens.debugScreen

import SettingsManager
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.design.fonts.headingLStyle

@Composable
fun DebugScreenDataStoreTab(
    modifier: Modifier = Modifier,
    data: SettingsDataAndroid,
    footerHeight: Dp,
    onClearDumpPath: () -> Unit,
    onRemoveDestinationClicked: (SettingsManager.Destination) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp),
    ) {
        Text(
            text = "Data Store",
            style = headingLStyle,
        )
        HMSpacerHeight(height = 10)
        Row {
            Column {
                Text(text = "'defaultPort':")
                Text(text = "'temporaryPort':")
                Text(text = "'serverPassword':")
                Text(text = "'nextAutoJobName':")
                Text(text = "'DumpPath.dumpUriPath':")
                Text(text = "'DumpPath.dumpPathReadable':")
                Text(text = "'DumpPath.dumpPathForReal':")
                Text(text = "'DumpPath.isSet':")
                Text(text = "'destinations':")
            }
            HMSpacerWidth(width = 10)
            Column {
                Text(text = data.defaultPort.toString())
                Text(text = data.temporaryPort.toString())
                Text(text = data.serverPassword.toString())
                Text(text = data.nextAutoJobName.toString())
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
                Text(text = data.dumpPath.isSet.toString())
            }
        }
        data.destinations.forEachIndexed { index, destination ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .background(
                            color = if (index % 2 == 0) {
                                Color.LightGray
                            } else {
                                Color.DarkGray
                            },
                        ),
                ) {
                    Text(text = "Name: ${destination.name}")
                    Text(text = "Password: ${destination.password}")
                    Text(text = "IP address: ${destination.ip}")
                }

                FittoniaIcon(
                    modifier = Modifier
                        .requiredSize(20.dp)
                        .clickable(onClick = { onRemoveDestinationClicked(destination) }),
                    drawableRes = R.drawable.ic_clear,
                )
            }
        }

        HMSpacerHeight(height = footerHeight)
    }
}
