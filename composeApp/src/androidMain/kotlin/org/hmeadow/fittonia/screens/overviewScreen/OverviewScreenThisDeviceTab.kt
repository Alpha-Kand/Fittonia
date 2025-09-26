package org.hmeadow.fittonia.screens.overviewScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.hmeadow.fittonia.androidServer.AndroidServer
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing2
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingMStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.utility.encodeIpAddress

@Composable
fun OverviewScreenThisDeviceTab(
    maxWidth: Dp,
    maxHeight: Dp,
    deviceIp: String,
    refreshIp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(width = maxWidth)
            .height(height = maxHeight)
            .padding(horizontal = spacing16),
    ) {
        FittoniaSpacerHeight(height = spacing16)

        ThisDeviceSection(deviceIp = deviceIp)

        FittoniaSpacerHeight(height = spacing32)

        ServerStatusSection()

        FittoniaSpacerHeight(height = spacing32)
    }
}

@Composable
private fun ThisDeviceSection(deviceIp: String) {
    Column {
        Text(
            text = "Device Info",
            style = headingMStyle,
        )

        FittoniaSpacerHeight(height = spacing16)

        Column(
            modifier = Modifier
                .border(
                    width = spacing2,
                    shape = RoundedCornerShape(size = spacing8),
                    color = currentStyle.headerBackgroundColour,
                )
                .padding(all = spacing8),
        ) {
            Row {
                Column {
                    Text(
                        text = "IP Address:",
                        style = paragraphTextStyle,
                    )
                    Text(
                        text = "IP Code:",
                        style = paragraphTextStyle,
                    )
                }
                FittoniaSpacerWidth(width = spacing16)
                Column {
                    Row {
                        Text(
                            text = deviceIp,
                            style = paragraphTextStyle,
                        )
                        FittoniaSpacerWidth(width = spacing8)
                        /* TODO make nice refresh icon for this.
                        Text(
                            modifier = Modifier.clickable(onClick = refreshIp),
                            text = "Refresh",
                            color = Color.Cyan,
                        )
                         */
                    }
                    Row {
                        Text(
                            text = encodeIpAddress(ipAddress = deviceIp),
                            style = paragraphTextStyle,
                        )
                        FittoniaSpacerWidth(width = spacing8)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerStatusSection() {
    Column {
        Text(
            text = "Server Status",
            style = headingMStyle,
        )

        FittoniaSpacerHeight(height = spacing16)

        Column(
            modifier = Modifier
                .border(
                    width = spacing2,
                    shape = RoundedCornerShape(size = spacing8),
                    color = currentStyle.headerBackgroundColour,
                )
                .padding(all = spacing8),
        ) {
            Row {
                Column {
                    Text(
                        text = "Status:",
                        style = paragraphTextStyle,
                    )
                }
                Column {
                    Text(
                        text = when (AndroidServer.server.collectAsState().value) { // TODO status for starting server
                            null -> "Offline ❌"
                            else -> "Online ✅"
                        },
                        style = paragraphTextStyle,
                    )
                }
            }
            // TODO restart server button.
        }
    }
}
