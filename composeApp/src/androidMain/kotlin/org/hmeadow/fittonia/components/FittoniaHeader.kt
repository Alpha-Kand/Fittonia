package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.BuildConfig
import org.hmeadow.fittonia.Navigator
import org.hmeadow.fittonia.R

@Composable
fun FittoniaHeader(
    headerText: String,
    onBackClicked: (() -> Unit)? = null,
    onOptionsClicked: (() -> Unit)? = null,
) {
    Box(modifier = Modifier.padding(all = 5.dp)) {
        onBackClicked?.let {
            FittoniaIcon(
                modifier = Modifier
                    .align(CenterStart)
                    .padding(5.dp)
                    .clickable(onClick = onBackClicked),
                drawableRes = R.drawable.ic_back_arrow,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Center),
        ) {
            HMSpacerWeightRow()
            Text(
                text = headerText,
                style = headerStyle,
            )
            HMSpacerWeightRow()
        }
        Row(modifier = Modifier.align(CenterEnd)) {
            onOptionsClicked?.let {
                Box(
                    modifier = Modifier
                        .requiredSize(40.dp)
                        .border(1.dp, FittoniaButtonType.Primary.borderColour, CircleShape)
                        .background(FittoniaButtonType.Primary.backgroundColor, CircleShape)
                        .clickable(onClick = onOptionsClicked),
                    contentAlignment = Center,
                ) { Text("•••", color = FittoniaButtonType.Primary.contentColour) }
            }
            if (BuildConfig.DEBUG) {
                HMSpacerWidth(width = 5)
                FittoniaIcon(
                    modifier = Modifier.clickable(onClick = Navigator::goToDebugScreen),
                    drawableRes = R.drawable.ic_debug,
                    tint = Color.Cyan,
                )
            }
        }
    }
}
