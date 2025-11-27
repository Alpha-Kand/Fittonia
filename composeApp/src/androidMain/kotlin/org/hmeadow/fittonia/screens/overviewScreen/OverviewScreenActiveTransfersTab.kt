package org.hmeadow.fittonia.screens.overviewScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.models.CompletedJob
import org.hmeadow.fittonia.utility.Debug
import org.hmeadow.fittonia.utility.Off

@Composable
fun OverviewScreenActiveTransfersTab(
    maxWidth: Dp,
    maxHeight: Dp,
    onTransferJobClicked: (Int) -> Unit,
    completedJobs: List<CompletedJob>,
    addNewDebugJob: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .width(maxWidth)
            .height(maxHeight),
    ) {
        FittoniaSpacerHeight(height = spacing32)

        OverviewTransferList(completedJobs = completedJobs, onTransferJobClicked = onTransferJobClicked)

        Debug(Off) {
            FittoniaButton(
                onClick = addNewDebugJob,
                type = currentStyle.primaryButtonType,
                content = { ButtonText(text = "<Add line>") },
            )
        }

        FittoniaSpacerHeight(height = spacing32)
    }
}
