package org.hmeadow.fittonia.screens.overviewScreen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.AndroidServer
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.VerticalLine
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.design.fonts.headingSStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldSmallTextStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldTextStyle
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus

@Composable
fun TransferList(
    onTransferJobClicked: (TransferJob) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = currentStyle.readOnlyBorderColour)
            .background(color = currentStyle.readOnlyBackgroundColour),
    ) {
        val maxProgressWidth = measureTextWidth(text = "100.0%", style = readOnlyFieldTextStyle)
        val maxStatusWidth = measureTextWidth(text = "Status", style = readOnlyFieldTextStyle)

        HeaderRow(
            maxProgressWidth = maxProgressWidth,
            maxStatusWidth = maxStatusWidth,
        )

        val androidServer = AndroidServer.server.collectAsState().value
        val transferJobs = androidServer?.transferJobs?.collectAsState()?.value ?: emptyList()
        transferJobs.forEachIndexed { index, job ->
            Row(
                modifier = Modifier
                    .requiredHeight(30.dp)
                    .padding(horizontal = 5.dp)
                    .clickable { onTransferJobClicked(job) },
                verticalAlignment = CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1.0f),
                    text = job.description,
                    style = readOnlyFieldSmallTextStyle,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )
                VerticalLine()
                Column(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .requiredWidth(maxProgressWidth),
                    horizontalAlignment = CenterHorizontally,
                ) {
                    Text(
                        text = rememberPercentageFormat(job.progressPercentage, minFraction = 1, maxFraction = 1),
                        style = readOnlyFieldTextStyle,
                    )
                }
                VerticalLine()
                Column(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .requiredWidth(maxStatusWidth),
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

@Composable
private fun HeaderRow(
    maxProgressWidth: Dp,
    maxStatusWidth: Dp,
) {
    Column {
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
                    .requiredWidth(maxProgressWidth),
                drawableRes = R.drawable.ic_send,
            )
            VerticalLine()
            Text(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .requiredWidth(maxStatusWidth),
                style = headingSStyle,
                text = "Status",
            )
            VerticalLine()
            FittoniaSpacerWidth(width = 20)
        }
        Box(
            modifier = Modifier
                .background(currentStyle.readOnlyBackgroundColour)
                .requiredHeight(2.dp)
                .fillMaxWidth(),
        ) {}
    }
}
