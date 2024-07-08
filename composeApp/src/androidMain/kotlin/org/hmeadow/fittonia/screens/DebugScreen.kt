package org.hmeadow.fittonia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.FittoniaBackground
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.headingLStyle

@Composable
fun DebugScreen(
    data: SettingsDataAndroid,
    onBackClicked: () -> Unit,
) {
    FittoniaBackground(
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
            Column(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(all = 16.dp),
            ) {
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
                            Text(text = data.dumpPath)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun SettingsLine(tag: String, value: String) {
    Row {
        Text(text = tag)
        HMSpacerWeightRow()
        //HMSpacerWidth(width = 10)
        Text(text = value)
    }
}
