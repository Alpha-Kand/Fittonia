package org.hmeadow.fittonia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaComingSoon
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.VerticalLine
import org.hmeadow.fittonia.components.headingSStyle
import org.hmeadow.fittonia.components.paragraphStyle
import java.text.NumberFormat
import java.util.Locale

@Composable
fun rememberPercentageFormat(
    percentage: Double,
    minFraction: Int? = null,
    maxFraction: Int? = 2,
): String = remember(
    percentage,
    minFraction,
    maxFraction,
) {
    NumberFormat.getPercentInstance(Locale.getDefault()).apply {
        maxFraction?.let { maximumFractionDigits = maxFraction }
        minFraction?.let { minimumFractionDigits = minFraction }
    }.format(percentage)
}

class Options(
    val name: String,
    val onClick: () -> Unit,
)

data class TransferJob(
    val description: String,
    val destination: String,
    val items: Int,
    val progress: Double,
    val status: TransferStatus,
    val direction: Direction,
) {
    enum class Direction {
        INCOMING,
        OUTGOING,
    }
}

enum class TransferStatus {
    Sending,
    Receiving,
    Error,
    Done,
}

val transferJobs = listOf(
    TransferJob(
        description = "Job 1",
        destination = "Home Computer",
        items = 36,
        progress = 0.30,
        status = TransferStatus.Sending,
        direction = TransferJob.Direction.OUTGOING,
    ),
    TransferJob(
        description = "PDFs",
        destination = "Bob at Work",
        items = 5,
        progress = 0.05,
        status = TransferStatus.Receiving,
        direction = TransferJob.Direction.INCOMING,
    ),
    TransferJob(
        description = "Problem",
        destination = "TOP SECRET",
        items = 1,
        progress = 0.05,
        status = TransferStatus.Error,
        direction = TransferJob.Direction.INCOMING,
    ),
    TransferJob(
        description = "Foo",
        destination = "192.56.43.01",
        items = 1,
        progress = 1.0,
        status = TransferStatus.Done,
        direction = TransferJob.Direction.INCOMING,
    ),
)

@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

@Composable
fun OverviewScreen(sendFiles: () -> Unit) {
    var optionsState by remember { mutableStateOf(false) }
    var aboutState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = "Ongoing transfers",
                onOptionsClicked = { optionsState = true },
            )
        },
        content = {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                HMSpacerHeight(height = 15)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE)),
                ) {
                    val progressWidth = measureTextWidth(text = "100%", style = headingSStyle)
                    val statusWidth = measureTextWidth(text = "Status", style = headingSStyle)

                    Row(
                        modifier = Modifier
                            .requiredHeight(30.dp)
                            .padding(horizontal = 5.dp),
                        verticalAlignment = CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1.0f),
                            style = headingSStyle,
                            text = "Transfer",
                        )
                        VerticalLine()
                        FittoniaIcon(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .requiredWidth(progressWidth),
                            drawableRes = R.drawable.ic_send,
                        )
                        VerticalLine()
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .requiredWidth(statusWidth),
                            style = headingSStyle,
                            text = "Status",
                        )
                        VerticalLine()
                        HMSpacerWidth(width = 20)
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF999999))
                            .requiredHeight(2.dp)
                            .fillMaxWidth(),
                    ) {}

                    transferJobs.forEachIndexed { index, job ->
                        Row(
                            modifier = Modifier
                                .requiredHeight(30.dp)
                                .padding(horizontal = 5.dp),
                            verticalAlignment = CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1.0f),
                                text = job.description,
                            )
                            VerticalLine()
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .requiredWidth(progressWidth),
                                horizontalAlignment = CenterHorizontally,
                            ) {
                                Text(
                                    text = rememberPercentageFormat(job.progress),
                                )
                            }
                            VerticalLine()
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .requiredWidth(statusWidth),
                                horizontalAlignment = CenterHorizontally,
                            ) {
                                FittoniaIcon(
                                    modifier = Modifier.padding(3.dp),
                                    drawableRes = when (job.status) {
                                        TransferStatus.Sending -> R.drawable.ic_arrow_send
                                        TransferStatus.Receiving -> R.drawable.ic_arrow_receive
                                        TransferStatus.Error -> R.drawable.ic_alert
                                        TransferStatus.Done -> R.drawable.ic_checkmark
                                    },
                                    tint = when (job.status) {
                                        TransferStatus.Sending -> Color(0xFF0000FF)
                                        TransferStatus.Receiving -> Color(0xFF0000FF)
                                        TransferStatus.Error -> Color(0xFFFFCC44)
                                        TransferStatus.Done -> Color(0xFF00FF00)
                                    },
                                )
                            }
                            VerticalLine()
                            FittoniaIcon(
                                modifier = Modifier
                                    .requiredWidth(20.dp)
                                    .padding(3.dp),
                                drawableRes = R.drawable.ic_chevron_right,
                            )
                        }
                        if (index != transferJobs.lastIndex) {
                            HorizontalLine()
                        }
                    }
                }
            }
        },
        footer = {
            Footer {
                Row {
                    FittoniaButton(
                        modifier = Modifier.weight(1.0f),
                        onClick = sendFiles,
                        content = { ButtonText(text = "Send files") },
                    )
                    HMSpacerWidth(20)
                    FittoniaComingSoon(
                        modifier = Modifier.weight(1.0f),
                    ) {
                        FittoniaButton(
                            onClick = { /*TODO*/ },
                            content = { ButtonText(text = "Send Message") },
                        )
                    }
                }
            }
        },
        overlay = {
            FittoniaModal(
                state = aboutState,
                onDismiss = { aboutState = false },
            ) { _ ->
                Text(
                    text = "Made by me :3",
                )
            }
            FittoniaModal(
                state = optionsState,
                onDismiss = { optionsState = false },
            ) { onDismiss ->
                listOf(
                    Options(
                        name = "Settings",
                        onClick = {},
                    ),
                    Options(
                        name = "About",
                        onClick = {
                            aboutState = true
                        },
                    ),
                ).forEach {
                    if (it.name == "Settings") {
                        FittoniaComingSoon {
                            Row {
                                Text(
                                    text = it.name,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    style = paragraphStyle,
                                )
                                HMSpacerWeightRow()
                                FittoniaIcon(
                                    modifier = Modifier.align(CenterVertically),
                                    drawableRes = R.drawable.ic_chevron_right,
                                    tint = Color(0xFF222222),
                                )
                            }
                        }
                    } else {
                        Row {
                            Text(
                                text = it.name,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                                    .clickable(
                                        onClick = {
                                            onDismiss()
                                            it.onClick()
                                        },
                                    ),
                                style = paragraphStyle,
                            )
                            HMSpacerWeightRow()
                            FittoniaIcon(
                                modifier = Modifier.align(CenterVertically),
                                drawableRes = R.drawable.ic_chevron_right,
                                tint = Color(0xFF222222),
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
@Preview
private fun Preview() {
    OverviewScreen { }
}
