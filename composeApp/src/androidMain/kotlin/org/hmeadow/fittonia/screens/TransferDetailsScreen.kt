package org.hmeadow.fittonia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.AndroidServer
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingMStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldTextStyle
import org.hmeadow.fittonia.screens.overviewScreen.TransferJob
import org.hmeadow.fittonia.screens.overviewScreen.TransferStatus
import org.hmeadow.fittonia.screens.overviewScreen.rememberPercentageFormat
import kotlin.random.Random

@Composable
fun TransferDetailsScreen(
    transferJob: TransferJob,
    onBackClicked: () -> Unit,
) {
    val androidServer = AndroidServer.server.collectAsState().value
    val transferJobs = androidServer?.transferJobs?.collectAsState()?.value ?: emptyList()
    val activeTransferJob = transferJobs.first { it.id == transferJob.id }

    FittoniaScaffold(
        header = {
            FittoniaHeader(
                onBackClicked = onBackClicked,
            )
        },
        content = {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Text(
                    text = activeTransferJob.description,
                    style = headingLStyle,
                )

                HMSpacerHeight(height = 40)

                Text(
                    text = "Destination",
                    style = headingMStyle,
                )

                HMSpacerHeight(height = 5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp),
                ) {
                    Text(
                        text = activeTransferJob.destination.name,
                        style = readOnlyFieldTextStyle,
                    )
                }
                HMSpacerHeight(height = 30)

                Text(
                    text = "Status",
                    style = headingMStyle,
                )

                HMSpacerHeight(height = 5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp),
                ) {
                    Row(
                        modifier = Modifier.requiredHeight(30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FittoniaIcon(
                            drawableRes = when (activeTransferJob.status) {
                                TransferStatus.Sending -> R.drawable.ic_arrow_send
                                TransferStatus.Receiving -> R.drawable.ic_arrow_receive
                                TransferStatus.Error -> R.drawable.ic_alert
                                TransferStatus.Done -> R.drawable.ic_checkmark
                            },
                            tint = when (activeTransferJob.status) {
                                TransferStatus.Sending -> Color(0xFF0000FF)
                                TransferStatus.Receiving -> Color(0xFF0000FF)
                                TransferStatus.Error -> Color(0xFFFFFF00)
                                TransferStatus.Done -> Color(0xFF00FF00)
                            },
                        )
                        HMSpacerWidth(width = 10)

                        Text(
                            text = when (activeTransferJob.status) {
                                TransferStatus.Sending -> "Sending"
                                TransferStatus.Receiving -> "Receiving"
                                TransferStatus.Error -> "Error"
                                TransferStatus.Done -> "Done!"
                            },
                            style = readOnlyFieldTextStyle,
                        )
                    }
                }
                HMSpacerHeight(height = 30)

                Text(
                    text = "Progress",
                    style = headingMStyle,
                )

                HMSpacerHeight(height = 5)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp),
                ) {
                    val aaa = when (activeTransferJob.status) {
                        TransferStatus.Sending -> "sent"
                        TransferStatus.Receiving -> "received"
                        else -> ""
                    }
                    Row {
                        Text(
                            text = stringResource(
                                id = R.string.transfer_details_screen_progress,
                                rememberPercentageFormat(
                                    percentage = activeTransferJob.progressPercentage,
                                    maxFraction = 2,
                                ),
                                activeTransferJob.currentItem,
                                activeTransferJob.totalItems,
                                aaa,
                            ),
                            style = readOnlyFieldTextStyle,
                        )
                    }
                }

                HMSpacerHeight(height = 30)

                Text(
                    text = "Logs",
                    style = headingMStyle,
                )

                HMSpacerHeight(height = 5)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp),
                ) {
                    activeTransferJob.items.forEachIndexed { index, file ->
                        var pathExpandedState by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 30.dp)
                                .clickable { pathExpandedState = !pathExpandedState }
                                .fillMaxWidth(),
                        ) {
                            val transferStatus = when (activeTransferJob.status) {
                                TransferStatus.Sending -> ""
                                TransferStatus.Receiving -> ""
                                TransferStatus.Error -> " Error"
                                TransferStatus.Done -> " Done"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (pathExpandedState) {
                                    Text(
                                        text = file.name + " todo long path?",
                                    )
                                    HMSpacerWeightRow()
                                } else {
                                    Text(
                                        text = stringResource(
                                            id = R.string.transfer_details_screen_item_transfer_status,
                                            file.name,
                                            transferStatus,
                                        ),
                                        style = readOnlyFieldTextStyle,
                                    )
                                    HMSpacerWeightRow()
                                    Text(
                                        text = rememberPercentageFormat(Random.nextDouble(1.0)),
                                        style = readOnlyFieldTextStyle,
                                    )
                                }
                                FittoniaIcon(
                                    modifier = Modifier.requiredHeight(20.dp),
                                    drawableRes = if (pathExpandedState) {
                                        R.drawable.ic_chevron_up
                                    } else {
                                        R.drawable.ic_chevron_down
                                    },
                                    tint = Color(0xFF222222),
                                )
                            }
                            if (pathExpandedState) {
                                Row {
                                    Text(
                                        text = "$transferStatus${0.0}",
                                        style = readOnlyFieldTextStyle,
                                    )
                                    HMSpacerWeightRow()
                                    Text(
                                        text = "${1000}b/${2000}b",
                                        style = readOnlyFieldTextStyle,
                                    )
                                }
                            }
                        }
                        if (index != activeTransferJob.items.lastIndex) {
                            HorizontalLine()
                        }
                    }
                }
            }
        },
        footer = {
            // TODO TranferDetailsFooter()
        },
    )
}

@Composable
private fun TranferDetailsFooter() {
    Column {
        Row {
            FittoniaButton(
                modifier = Modifier.weight(1f),
                onClick = { /*TODO*/ },
            ) {
                ButtonText(text = "Queue \uD83D\uDD03")
            }
            HMSpacerWidth(width = 5)
            FittoniaButton(
                modifier = Modifier.weight(1f),
                onClick = { /*TODO*/ },
            ) {
                ButtonText(text = "Cancel ❌")
            }
        }
        FittoniaButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /*TODO*/ },
        ) {
            ButtonText(text = "Pause ⏸\uFE0F")
        }
    }
}
