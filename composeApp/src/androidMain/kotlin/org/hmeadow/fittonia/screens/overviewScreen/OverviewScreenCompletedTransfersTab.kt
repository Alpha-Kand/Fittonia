package org.hmeadow.fittonia.screens.overviewScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.VerticalLine
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing2
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingSStyle
import org.hmeadow.fittonia.design.fonts.readOnlyFieldSmallTextStyle
import org.hmeadow.fittonia.models.CompletedJob

@Composable
fun OverviewScreenCompletedTransfersTab(
    maxWidth: Dp,
    maxHeight: Dp,
    completedJobs: List<CompletedJob>,
) {
    Column(
        modifier = Modifier
            .width(width = maxWidth)
            .height(height = maxHeight),
    ) {
        val itemCountWidth = measureTextWidth(text = "Item #", style = headingSStyle)
        FittoniaSpacerHeight(height = spacing32)
        Column(
            modifier = Modifier
                .padding(horizontal = spacing16)
                .border(
                    width = spacing2,
                    color = currentStyle.readOnlyBorderColour,
                    shape = RoundedCornerShape(size = 5.dp),
                )
                .background(
                    color = currentStyle.readOnlyBackgroundColour,
                    shape = RoundedCornerShape(size = 5.dp),
                ),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = spacing8)
                    .requiredHeight(height = spacing32),
                verticalAlignment = CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1.0f),
                    text = "Description",
                    style = headingSStyle,
                )
                VerticalLine()
                Text(
                    modifier = Modifier
                        .padding(start = spacing4)
                        .requiredWidth(itemCountWidth),
                    text = "Item #",
                    style = headingSStyle,
                )
            }
            HorizontalLine()
            if (completedJobs.isEmpty()) {
                CompleteTransferRow(description = "No transfers received", itemCountWidth = itemCountWidth)
                HorizontalLine()
                CompleteTransferRow(itemCountWidth = itemCountWidth)
                HorizontalLine()
                CompleteTransferRow(itemCountWidth = itemCountWidth)
            }
            completedJobs.forEachIndexed { index, job ->
                CompleteTransferRow(
                    description = job.description,
                    itemCount = "${job.items.size}",
                    itemCountWidth = itemCountWidth,
                )
                if (index != completedJobs.lastIndex) {
                    HorizontalLine()
                }
            }
            if (completedJobs.size in 1..2) {
                repeat(times = 3 - completedJobs.size) {
                    HorizontalLine()
                    CompleteTransferRow(itemCountWidth = itemCountWidth)
                }
            }
        }
        FittoniaSpacerHeight(height = spacing32)
    }
}

@Composable
private fun CompleteTransferRow(
    description: String = "",
    itemCount: String = "",
    itemCountWidth: Dp,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = spacing8)
            .requiredHeight(height = spacing32),
        verticalAlignment = CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1.0f),
            text = description,
            style = readOnlyFieldSmallTextStyle,
        )
        VerticalLine()
        Text(
            modifier = Modifier
                .padding(start = spacing4)
                .requiredWidth(width = itemCountWidth),
            text = itemCount,
            style = readOnlyFieldSmallTextStyle,
            textAlign = TextAlign.Right,
        )
    }
}
