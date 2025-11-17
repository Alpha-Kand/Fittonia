package org.hmeadow.fittonia.screens.transferDetailsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.ReadOnlyEntries
import org.hmeadow.fittonia.compose.architecture.ComposeDataState
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.HorizontalScrollableContent
import org.hmeadow.fittonia.compose.architecture.LoadingCompose
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaIcon
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingMStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldTextStyle
import org.hmeadow.fittonia.models.TransferStatus
import org.hmeadow.fittonia.utility.bytesToMegaBytes
import org.hmeadow.fittonia.utility.measureTextWidth
import org.hmeadow.fittonia.utility.rememberPercentageFormat

internal class TransferDetailsScreenData(
    val description: String,
    val status: TransferStatus,
    val progressPercentage: Double,
    val currentItem: Int,
    val totalItems: Int,
    val bytesPerSecond: Long,
    val transferTarget: TransferTarget,
    val items: List<TransferDetailsScreenDataItem>,
)

internal class TransferDetailsScreenDataItem(
    val name: String,
    val progressPercentage: Double,
    val sizeBytes: Long,
    val progressBytes: Long,
)

@Composable
internal fun TransferDetailsScreen(
    transferJobState: ComposeDataState<TransferDetailsScreenData?>,
    onBackClicked: () -> Unit,
) {
    FittoniaScaffold(
        header = { FittoniaHeader(onBackClicked = onBackClicked) },
        content = {
            LoadingCompose(
                composeDataState = transferJobState,
                failureBlock = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Something went wrong!", // TODO - After release
                            style = headingLStyle,
                        )
                    }
                },
            ) { transferJobData ->
                if (transferJobData == null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Something went wrong!", // TODO - After release
                            style = headingLStyle,
                        )
                    }
                } else {
                    TransferDetailsScreenSuccessView(
                        transferJobData = transferJobData,
                    )
                }
            }
        },
        footer = {
            // TODO TranferDetailsFooter() - After release
        },
    )
}

sealed interface TransferTarget {
    class Out(val destinationName: String) : TransferTarget
    class In(val sourceName: String) : TransferTarget
}

@Composable
private fun TransferDetailsScreenSuccessView(transferJobData: TransferDetailsScreenData) {
    Column(modifier = Modifier.padding(all = spacing16)) {
        Text(
            text = transferJobData.description,
            style = headingLStyle,
        )

        FittoniaSpacerHeight(height = 40)

        when (transferJobData.transferTarget) {
            is TransferTarget.Out -> {
                Text(
                    text = stringResource(R.string.transfer_details_screen_destination_label),
                    style = headingMStyle,
                )
                FittoniaSpacerHeight(height = spacing4)
                ReadOnlyEntries(
                    entries = listOf(transferJobData.transferTarget.destinationName),
                    textStyle = readOnlyFieldTextStyle,
                )
            }

            is TransferTarget.In -> {
                Text(
                    text = stringResource(R.string.transfer_details_screen_source_label),
                    style = headingMStyle,
                )
                FittoniaSpacerHeight(height = spacing4)
                ReadOnlyEntries(
                    entries = listOf(transferJobData.transferTarget.sourceName),
                    textStyle = readOnlyFieldTextStyle,
                )
            }
        }
        FittoniaSpacerHeight(height = spacing32)

        Text(
            text = stringResource(R.string.transfer_details_screen_status_label),
            style = headingMStyle,
        )

        FittoniaSpacerHeight(height = spacing4)

        val statusComposable: @Composable () -> Unit = {
            Row(
                modifier = Modifier.requiredHeight(height = spacing32),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FittoniaIcon(
                    painter = painterResource(
                        when (transferJobData.status) {
                            TransferStatus.Sending -> R.drawable.ic_arrow_send
                            TransferStatus.Receiving -> R.drawable.ic_arrow_receive
                            TransferStatus.Error -> R.drawable.ic_alert
                            TransferStatus.Done -> R.drawable.ic_checkmark
                        },
                    ),
                    tint = when (transferJobData.status) {
                        TransferStatus.Sending -> Color(0xFF0000FF)
                        TransferStatus.Receiving -> Color(0xFF0000FF)
                        TransferStatus.Error -> Color(0xFFFFFF00)
                        TransferStatus.Done -> Color(0xFF00FF00)
                    },
                )
                FittoniaSpacerWidth(width = spacing8)

                Text(
                    text = when (transferJobData.status) {
                        TransferStatus.Sending -> "Sending"
                        TransferStatus.Receiving -> "Receiving"
                        TransferStatus.Error -> "Error"
                        TransferStatus.Done -> "Done!"
                    },
                    style = readOnlyFieldTextStyle,
                )
            }
        }

        ReadOnlyEntries(entries = listOf(statusComposable))

        FittoniaSpacerHeight(height = spacing32)

        Text(
            text = stringResource(R.string.transfer_details_screen_progress_label),
            style = headingMStyle,
        )

        FittoniaSpacerHeight(height = spacing4)

        val status = when (transferJobData.status) {
            TransferStatus.Sending -> "sent"
            TransferStatus.Receiving -> "received"
            else -> ""
        }
        ReadOnlyEntries(
            entries = listOf(
                stringResource(
                    id = R.string.transfer_details_screen_progress,
                    rememberPercentageFormat(
                        percentage = transferJobData.progressPercentage,
                        maxFraction = 2,
                    ),
                    transferJobData.currentItem,
                    transferJobData.totalItems,
                    status,
                ),
            ),
        )

        FittoniaSpacerHeight(height = spacing32)

        Text(
            text = stringResource(R.string.transfer_details_screen_logs_label),
            style = headingMStyle,
        )

        FittoniaSpacerHeight(height = spacing4)

        ReadOnlyEntries(
            entries = transferJobData.items.map { file ->
                val composable = @Composable {
                    ItemInfoBox(
                        status = transferJobData.status,
                        bytesPerSecond = transferJobData.bytesPerSecond,
                        file = file,
                    )
                }
                composable
            },
        )
    }
}

val TransferStatus.text: String
    get() = when (this) {
        TransferStatus.Done -> " Done"
        TransferStatus.Error -> " Error"
        TransferStatus.Sending -> "Sending"
        TransferStatus.Receiving -> "Receiving"
    }

@Composable
private fun ItemInfoBox(
    status: TransferStatus,
    bytesPerSecond: Long,
    file: TransferDetailsScreenDataItem,
) {
    val percentageTextWidth = measureTextWidth(text = "%100.00", style = readOnlyFieldTextStyle)
    var pathExpandedState by remember { mutableStateOf(value = false) }
    Column(
        modifier = Modifier
            .defaultMinSize(minHeight = spacing32)
            .clickable { pathExpandedState = !pathExpandedState }
            .fillMaxWidth()
            .background(color = currentStyle.readOnlyBackgroundColour), // TODO
    ) {
        HorizontalScrollableContent(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(height = spacing32),
            content = {
                Text(
                    text = stringResource(
                        id = R.string.transfer_details_screen_item_transfer_status,
                        file.name,
                        status.text,
                    ),
                    style = readOnlyFieldTextStyle,
                )
            },
            endContent = {
                Row(modifier = Modifier.requiredWidth(width = percentageTextWidth + spacing16)) {
                    Text(
                        modifier = Modifier.requiredWidth(width = percentageTextWidth),
                        textAlign = TextAlign.End,
                        text = rememberPercentageFormat(
                            percentage = file.progressPercentage,
                            minFraction = 1,
                            maxFraction = 2,
                        ),
                        style = readOnlyFieldTextStyle,
                    )
                    FittoniaIcon(
                        modifier = Modifier
                            .requiredHeight(height = spacing32)
                            .align(alignment = Alignment.CenterVertically),
                        painter = painterResource(
                            if (pathExpandedState) {
                                R.drawable.ic_chevron_up
                            } else {
                                R.drawable.ic_chevron_down
                            },
                        ),
                        tint = Color(0xFF222222), // TODO
                    )
                }
            },
        )
        if (pathExpandedState) {
            HorizontalScrollableContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(height = spacing32),
                content = {
                    val sizeBytes = bytesToMegaBytes(bytes = file.sizeBytes)
                    val statusTextWidth = measureTextWidth(
                        text = "$sizeBytes MB",
                        style = readOnlyFieldTextStyle,
                    )
                    Row {
                        Text(
                            modifier = Modifier.width(width = statusTextWidth * 3),
                            text = "${bytesToMegaBytes(bytes = file.progressBytes)}/$sizeBytes MB",
                            style = readOnlyFieldTextStyle,
                        )

                        FittoniaSpacerWidth(width = spacing32)

                        if (status != TransferStatus.Done) {
                            Text(
                                text = "${bytesToMegaBytes(bytes = bytesPerSecond)}MB/s",
                                style = readOnlyFieldTextStyle,
                            )
                        }
                    }
                },
                endContent = {
                    val text = "Status: ${status.text}"
                    val statusTextWidth: Dp = measureTextWidth(
                        text = text,
                        style = readOnlyFieldTextStyle,
                    )
                    Text(
                        modifier = Modifier.requiredWidth(width = statusTextWidth),
                        text = text,
                        style = readOnlyFieldTextStyle,
                    )
                },
            )
        }
    }
}

@Composable
private fun TranferDetailsFooter() {
    Column {
        Row {
            FittoniaButton(
                modifier = Modifier.weight(weight = 1f),
                onClick = { /*TODO - After release*/ },
            ) {
                ButtonText(text = "Queue \uD83D\uDD03")
            }
            FittoniaSpacerWidth(width = 5)
            FittoniaButton(
                modifier = Modifier.weight(weight = 1f),
                onClick = { /*TODO - After release*/ },
            ) {
                ButtonText(text = "Cancel ❌")
            }
        }
        FittoniaButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /*TODO - After release*/ },
        ) {
            ButtonText(text = "Pause ⏸\uFE0F")
        }
    }
}
