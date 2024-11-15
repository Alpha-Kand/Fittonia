package org.hmeadow.fittonia.screens.debugScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.psstStyle

@Composable
fun DebugScreenAdminCreateTab(
    nextAutoJobName: Long,
    nextAutoJobNameMessage: String,
    onCreateJobDirectory: () -> Unit,
    onCreateNewDestination: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = "AdminCreate",
            style = headingLStyle,
        )
        HMSpacerHeight(height = 10)
        if (nextAutoJobName >= 0) {
            Column {
                FittoniaButton(onClick = onCreateJobDirectory) {
                    ButtonText(text = "Create new job directory: \"Job$nextAutoJobName\"")
                }
                Text(
                    text = nextAutoJobNameMessage,
                    style = psstStyle,
                )
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.requiredSize(17.dp),
                color = Color.Black,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
            )
        }

        FittoniaButton(onClick = onCreateNewDestination) {
            ButtonText(text = "Create new destination")
        }
    }
}
