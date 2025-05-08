package org.hmeadow.fittonia.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.Navigator
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.design.fonts.headerStyle
import org.hmeadow.fittonia.utility.Debug
import org.hmeadow.fittonia.utility.InfoBorderState

@Composable
fun FittoniaHeader(
    headerText: String? = null,
    onBackClicked: (() -> Unit)? = null,
    onOptionsClicked: (() -> Unit)? = null,
    onAlertsClicked: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .background(
                color = currentStyle.headerBackgroundColour,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomEnd = 20.dp,
                    bottomStart = 20.dp,
                ),
            )
            .padding(horizontal = 15.dp)
            .padding(vertical = 5.dp),
        contentAlignment = Center,
    ) {
        Row(modifier = Modifier.align(CenterStart)) {
            onBackClicked?.let {
                FittoniaIcon(
                    modifier = Modifier
                        .padding(5.dp)
                        .clickable(onClick = onBackClicked),
                    drawableRes = R.drawable.ic_back_arrow,
                )
            }
            FittoniaIcon(
                modifier = Modifier
                    .padding(5.dp)
                    .clickable(onClick = InfoBorderState::enableInfoBorder),
                drawableRes = R.drawable.ic_info,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Center),
        ) {
            FittoniaSpacerWeightRow()
            headerText?.let {
                Text(
                    text = headerText,
                    style = headerStyle,
                )
                FittoniaSpacerWeightRow()
            }
        }
        Row(modifier = Modifier.align(CenterEnd)) {
            onOptionsClicked?.let {
                Box(
                    modifier = Modifier
                        .requiredSize(40.dp)
                        .border(1.dp, currentStyle.primaryButtonType.borderColour, CircleShape)
                        .background(currentStyle.primaryButtonType.backgroundColor, CircleShape)
                        .clickable(onClick = onOptionsClicked),
                    contentAlignment = Center,
                ) { Text(text = "•••", color = currentStyle.primaryButtonType.contentColour) }
            }
            onAlertsClicked?.let {
                Image(
                    modifier = Modifier.clickable(onClick = onAlertsClicked),
                    painter = painterResource(id = R.drawable.ic_warning_yellow),
                    contentDescription = "", // TODO - After release
                )
            }
            Debug {
                FittoniaSpacerWidth(width = 5)
                FittoniaIcon(
                    modifier = Modifier.clickable(onClick = Navigator::goToDebugScreen),
                    drawableRes = R.drawable.ic_debug,
                    tint = Color.Cyan,
                )
            }
        }
    }
}
