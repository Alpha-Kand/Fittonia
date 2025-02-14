package org.hmeadow.fittonia.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.hmeadow.fittonia.AndroidServer.Companion.server
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.ReadOnlyEntries
import org.hmeadow.fittonia.compose.architecture.ComposeDataState
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.LoadingCompose
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaIcon
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingMStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldTextStyle
import org.hmeadow.fittonia.models.IncomingJob
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus
import org.hmeadow.fittonia.screens.overviewScreen.rememberPercentageFormat
import kotlin.random.Random

class TransferDetailsScreenViewModel(private val transferJob: TransferJob) : BaseViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentTransferJob: Flow<TransferJob?> = server.flatMapLatest { eee ->
        eee?.transferJobs?.map { bbb ->
            bbb.firstOrNull { it.id == transferJob.id }
        } ?: flow { emit(null) }
    }
}

@Composable
fun TransferDetailsScreen(
    transferJobState: ComposeDataState<TransferJob?>,
    onBackClicked: () -> Unit,
) {
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                onBackClicked = onBackClicked,
            )
        },
        content = {
            LoadingCompose(
                composeDataState = transferJobState,
                failureBlock = {
                    Text(
                        text = "Something went wrong!", //TODO
                        style = headingLStyle,
                    )
                },
            ) { transferJob ->
                if (transferJob == null) {
                    Text(
                        text = "Something went wrong! 2", //TODO
                        style = headingLStyle,
                    )
                } else {
                    Column(modifier = Modifier.padding(all = 16.dp)) {
                        Text(
                            text = transferJob.description,
                            style = headingLStyle,
                        )

                        FittoniaSpacerHeight(height = 40)

                        when (transferJob) {
                            is OutgoingJob -> {
                                Text(
                                    text = "Destination",
                                    style = headingMStyle,
                                )
                                FittoniaSpacerHeight(height = 5)
                                ReadOnlyEntries(
                                    entries = listOf(transferJob.destination.name),
                                    textStyle = readOnlyFieldTextStyle,
                                )
                            }

                            is IncomingJob -> {
                                Text(
                                    text = "Source",
                                    style = headingMStyle,
                                )
                                FittoniaSpacerHeight(height = 5)
                                ReadOnlyEntries(
                                    entries = listOf(transferJob.source.name),
                                    textStyle = readOnlyFieldTextStyle,
                                )
                            }
                        }
                        FittoniaSpacerHeight(height = 30)

                        Text(
                            text = "Status",
                            style = headingMStyle,
                        )

                        FittoniaSpacerHeight(height = 5)

                        val statusComposable: @Composable () -> Unit = {
                            Row(
                                modifier = Modifier.requiredHeight(30.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FittoniaIcon(
                                    painter = painterResource(
                                        when (transferJob.status) {
                                            TransferStatus.Sending -> R.drawable.ic_arrow_send
                                            TransferStatus.Receiving -> R.drawable.ic_arrow_receive
                                            TransferStatus.Error -> R.drawable.ic_alert
                                            TransferStatus.Done -> R.drawable.ic_checkmark
                                        },
                                    ),
                                    tint = when (transferJob.status) {
                                        TransferStatus.Sending -> Color(0xFF0000FF)
                                        TransferStatus.Receiving -> Color(0xFF0000FF)
                                        TransferStatus.Error -> Color(0xFFFFFF00)
                                        TransferStatus.Done -> Color(0xFF00FF00)
                                    },
                                )
                                FittoniaSpacerWidth(width = 10)

                                Text(
                                    text = when (transferJob.status) {
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

                        FittoniaSpacerHeight(height = 30)

                        Text(
                            text = "Progress",
                            style = headingMStyle,
                        )

                        FittoniaSpacerHeight(height = 5)

                        val aaa = when (transferJob.status) {
                            TransferStatus.Sending -> "sent"
                            TransferStatus.Receiving -> "received"
                            else -> ""
                        }
                        ReadOnlyEntries(
                            entries = listOf(
                                stringResource(
                                    id = R.string.transfer_details_screen_progress,
                                    rememberPercentageFormat(
                                        percentage = transferJob.progressPercentage,
                                        maxFraction = 2,
                                    ),
                                    transferJob.currentItem,
                                    transferJob.totalItems,
                                    aaa,
                                ),
                            ),
                        )

                        FittoniaSpacerHeight(height = 30)

                        Text(
                            text = "Logs",
                            style = headingMStyle,
                        )

                        FittoniaSpacerHeight(height = 5)

                        ReadOnlyEntries(
                            entries = transferJob.items.mapIndexed { index, file ->
                                val composable = @Composable {
                                    var pathExpandedState by remember { mutableStateOf(false) }

                                    Column(
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 30.dp)
                                            .clickable { pathExpandedState = !pathExpandedState }
                                            .fillMaxWidth(),
                                    ) {
                                        val transferStatus = when (transferJob.status) {
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
                                                FittoniaSpacerWeightRow()
                                            } else {
                                                Text(
                                                    text = stringResource(
                                                        id = R.string.transfer_details_screen_item_transfer_status,
                                                        file.name,
                                                        transferStatus,
                                                    ),
                                                    style = readOnlyFieldTextStyle,
                                                )
                                                FittoniaSpacerWeightRow()
                                                Text(
                                                    text = rememberPercentageFormat(Random.nextDouble(1.0)),
                                                    style = readOnlyFieldTextStyle,
                                                )
                                            }
                                            FittoniaIcon(
                                                modifier = Modifier.requiredHeight(20.dp),
                                                painter = painterResource(
                                                    if (pathExpandedState) {
                                                        R.drawable.ic_chevron_up
                                                    } else {
                                                        R.drawable.ic_chevron_down
                                                    },
                                                ),
                                                tint = Color(0xFF222222),
                                            )
                                        }
                                        if (pathExpandedState) {
                                            Row {
                                                Text(
                                                    text = "$transferStatus${0.0}",
                                                    style = readOnlyFieldTextStyle,
                                                )
                                                FittoniaSpacerWeightRow()
                                                Text(
                                                    text = "${1000}b/${2000}b",
                                                    style = readOnlyFieldTextStyle,
                                                )
                                            }
                                        }
                                    }
                                    if (index != transferJob.items.lastIndex) {
                                        HorizontalLine()
                                    }
                                }
                                composable
                            },
                        )
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
            FittoniaSpacerWidth(width = 5)
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
