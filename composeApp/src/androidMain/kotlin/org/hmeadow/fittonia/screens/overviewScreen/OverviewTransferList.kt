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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.androidServer.AndroidServer
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.VerticalLine
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.appStyleResetReadOnly
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingSStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldSmallTextStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldTextStyle
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus

@Composable
fun OverviewTransferList(
    onTransferJobClicked: (TransferJob) -> Unit,
    modifier: Modifier = Modifier,
) {
    key(appStyleResetReadOnly) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = currentStyle.readOnlyBorderColour,
                    shape = RoundedCornerShape(size = 5.dp),
                )
                .background(color = currentStyle.readOnlyBackgroundColour, shape = RoundedCornerShape(size = 5.dp)),
        ) {
            val maxProgressWidth = measureTextWidth(text = "100.0%", style = readOnlyFieldTextStyle)
            val maxStatusWidth = measureTextWidth(text = "Status", style = readOnlyFieldTextStyle)

            val androidServer = AndroidServer.server.collectAsState().value
            val transferJobs = androidServer?.transferJobs?.collectAsState()?.value ?: emptyList()

            HeaderRow(
                noTransfers = transferJobs.isEmpty(),
                maxProgressWidth = maxProgressWidth,
                maxStatusWidth = maxStatusWidth,
            )

            if (transferJobs.isEmpty()) {
                TransferRow(text = "No transfers active.", hideChevronColumn = true)
                HorizontalLine()
                TransferRow(hideChevronColumn = true)
                HorizontalLine()
                TransferRow(hideChevronColumn = true)
            }
            transferJobs.forEachIndexed { index, job ->
                TransferRow(
                    text = job.description,
                    onClick = { onTransferJobClicked(job) },
                    progress = job.progressPercentage,
                    status = job.status,
                )
                if (index != transferJobs.lastIndex) {
                    HorizontalLine()
                }
            }

            if (transferJobs.size in 1..2) {
                repeat(times = 3 - transferJobs.size) {
                    HorizontalLine()
                    TransferRow()
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(
    noTransfers: Boolean,
    maxProgressWidth: Dp,
    maxStatusWidth: Dp,
) {
    Column {
        Row(
            modifier = Modifier
                .requiredHeight(height = spacing32)
                .padding(horizontal = spacing8),
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
            if (!noTransfers) {
                VerticalLine()
                FittoniaSpacerWidth(width = 20)
            }
        }
        HorizontalLine()
    }
}

@Composable
private fun TransferRow(
    text: String? = null,
    onClick: (() -> Unit)? = null,
    progress: Double? = null,
    status: TransferStatus? = null,
    hideChevronColumn: Boolean = false,
) {
    val maxProgressWidth = measureTextWidth(text = "100.0%", style = readOnlyFieldTextStyle)
    val maxStatusWidth = measureTextWidth(text = "Status", style = readOnlyFieldTextStyle)
    Row(
        modifier = Modifier
            .requiredHeight(height = spacing32)
            .padding(horizontal = spacing8)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalAlignment = CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1.0f),
            text = text ?: "",
            style = readOnlyFieldSmallTextStyle,
            overflow = TextOverflow.Ellipsis,
        )
        VerticalLine()
        Column(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .requiredWidth(width = maxProgressWidth),
            horizontalAlignment = CenterHorizontally,
        ) {
            progress?.let {
                Text(
                    text = rememberPercentageFormat(percentage = it, minFraction = 1, maxFraction = 1),
                    style = readOnlyFieldTextStyle,
                )
            }
        }
        VerticalLine()
        Column(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .requiredWidth(maxStatusWidth),
            horizontalAlignment = CenterHorizontally,
        ) {
            status?.let {
                FittoniaIcon(
                    modifier = Modifier.padding(all = 3.dp),
                    drawableRes = when (it) {
                        TransferStatus.Sending -> R.drawable.ic_arrow_send
                        TransferStatus.Receiving -> R.drawable.ic_arrow_receive
                        TransferStatus.Error -> R.drawable.ic_alert
                        TransferStatus.Done -> R.drawable.ic_checkmark
                    },
                    tint = when (it) {
                        TransferStatus.Sending -> Color(0xFF0000FF)
                        TransferStatus.Receiving -> Color(0xFF0000FF)
                        TransferStatus.Error -> Color(0xFFFFCC44)
                        TransferStatus.Done -> Color(0xFF00FF00)
                    },
                )
            }
        }
        if (!hideChevronColumn) {
            VerticalLine()
            if (onClick != null) {
                FittoniaIcon(
                    modifier = Modifier
                        .requiredWidth(width = 20.dp)
                        .padding(all = 3.dp),
                    drawableRes = R.drawable.ic_chevron_right,
                )
            } else {
                Box(
                    modifier = Modifier.requiredWidth(width = 20.dp),
                ) {}
            }
        }
    }
}
