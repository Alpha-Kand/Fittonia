package org.hmeadow.fittonia.screens

import SettingsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.headingLStyle
import org.hmeadow.fittonia.components.headingSStyle

@Composable
fun DebugScreen(
    data: SettingsDataAndroid,
    onResetSettingsClicked: () -> Unit,
    onClearDumpPath: () -> Unit,
    onRemoveDestinationClicked: (SettingsManager.Destination) -> Unit,
    onBackClicked: () -> Unit,
) {
    FittoniaScaffold(
        header = {
            Box {
                FittoniaIcon(
                    modifier = Modifier
                        .padding(5.dp)
                        .clickable(onClick = onBackClicked),
                    drawableRes = R.drawable.ic_back_arrow,
                )
                Row(Modifier.fillMaxWidth()) {
                    HMSpacerWeightRow()
                    Text(
                        text = "Debug Screen",
                        style = headingLStyle,
                    )
                    HMSpacerWeightRow()
                }
            }
        },
        content = {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Row {
                    Column {
                        Text(text = "Default Server Port:")
                        Text(text = "Server Password:")
                        Text(text = "Dump Path:")
                    }
                    HMSpacerWidth(width = 10)
                    Column {
                        Text(text = data.defaultPort.toString())
                        Text(text = data.serverPassword.toString())
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .background(Color.LightGray),
                        ) {
                            Text(text = data.dumpPath.dumpPathUri)

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
                    }
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
                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onResetSettingsClicked,
                ) {
                    ButtonText(text = "Reset Settings")
                }
            }
        },
    )
}
