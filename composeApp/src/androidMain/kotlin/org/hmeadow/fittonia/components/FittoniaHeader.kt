package org.hmeadow.fittonia.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.MainActivity.Companion.mainActivity
import org.hmeadow.fittonia.Navigator
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.HeaderAndFooterDrawingConstants.BORDER_WIDTH
import org.hmeadow.fittonia.components.HeaderAndFooterDrawingConstants.CORNER_RADIUS
import org.hmeadow.fittonia.components.HeaderAndFooterDrawingConstants.shadowColours
import org.hmeadow.fittonia.compose.architecture.DebugAppStyle
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaCircleButton
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.fonts.headerStyle
import org.hmeadow.fittonia.utility.Debug
import org.hmeadow.fittonia.utility.InfoBorderState
import org.hmeadow.fittonia.utility.dpToFloat

@Composable
fun FittoniaHeader(
    headerText: String? = null,
    includeInfoButton: Boolean = false,
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
                    bottomEnd = CORNER_RADIUS.dp,
                    bottomStart = CORNER_RADIUS.dp,
                ),
            )
            .drawWithCache {
                onDrawBehind {
                    headerDraw()
                }
            }
            .padding(horizontal = spacing16)
            .padding(vertical = spacing4),
        contentAlignment = Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(
                onBackClicked = {
                    InfoBorderState.clearInfoBorderState()
                    onBackClicked?.invoke()
                },
            )
            if (includeInfoButton) {
                InfoButton()
            }
            FittoniaSpacerWeightRow()
            HeaderHeading(headerText = headerText)
            if (headerText != null) {
                FittoniaSpacerWeightRow()
            }
            OptionsButton(onOptionsClicked = onOptionsClicked)
            AlertsButton(onAlertsClicked = onAlertsClicked)
            Debug {
                FittoniaSpacerWidth(width = 5)
                DebugMenuButton()
            }
        }
    }
}

@Composable
private fun BackButton(onBackClicked: () -> Unit) {
    FittoniaCircleButton(onClick = onBackClicked) {
        CircleButtonIcon(drawableRes = R.drawable.ic_back_arrow)
    }
}

@Composable
private fun InfoButton() {
    FittoniaCircleButton(onClick = InfoBorderState::enableInfoBorder) {
        CircleButtonIcon(drawableRes = R.drawable.ic_info)
    }
}

@Composable
private fun HeaderHeading(headerText: String?) {
    headerText?.let {
        Text(
            text = headerText,
            style = headerStyle,
            color = DebugAppStyle.headerTextColour,
        )
    }
}

@Composable
private fun OptionsButton(onOptionsClicked: (() -> Unit)?) {
    onOptionsClicked?.let {
        FittoniaCircleButton(onClick = onOptionsClicked) {
            CircleButtonIcon(drawableRes = R.drawable.ic_options)
        }
    }
}

@Composable
private fun AlertsButton(onAlertsClicked: (() -> Unit)?) {
    onAlertsClicked?.let {
        Image(
            modifier = Modifier.clickable(onClick = onAlertsClicked),
            painter = painterResource(id = R.drawable.ic_warning_yellow),
            contentDescription = "", // TODO - After release
        )
    }
}

@Composable
private fun DebugMenuButton() {
    FittoniaIcon(
        modifier = Modifier.clickable(onClick = Navigator::goToDebugScreen),
        drawableRes = R.drawable.ic_debug,
        tint = Color.Cyan,
    )
}

private fun DrawScope.headerDraw() {
    val cornerRadius = mainActivity.dpToFloat(CORNER_RADIUS.dp)
    val cornerDiameter = cornerRadius * 2
    val cornerSize = Size(width = cornerDiameter, height = cornerDiameter)
    val width = this.size.width
    val height = this.size.height

    // Left corner.
    run {
        // Shadow.
        drawArc(
            brush = Brush.radialGradient(
                colors = shadowColours,
                center = Offset(x = cornerRadius, y = height - cornerRadius),
            ),
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true,
            size = cornerSize,
            topLeft = Offset(x = 0f, y = height - cornerDiameter),
        )

        // Border.
        drawArc(
            color = currentStyle.headerAndFooterBorderColour,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            size = cornerSize,
            topLeft = Offset(x = 0f, y = height - cornerDiameter),
            style = Stroke(width = BORDER_WIDTH),
        )
    }

    // Right corner.
    run {
        // Shadow.
        drawArc(
            brush = Brush.radialGradient(
                colors = shadowColours,
                center = Offset(x = width - cornerRadius, y = height - cornerRadius),
            ),
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true,
            size = cornerSize,
            topLeft = Offset(x = width - cornerDiameter, y = height - cornerDiameter),
        )
        // Border.
        drawArc(
            color = currentStyle.headerAndFooterBorderColour,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            size = cornerSize,
            topLeft = Offset(x = width - cornerDiameter, y = height - cornerDiameter),
            style = Stroke(width = BORDER_WIDTH),
        )
    }

    // Bottom side.
    run {
        // Shadow.
        drawRect(
            brush = Brush.linearGradient(
                colors = shadowColours,
                start = Offset(x = 0f, y = height - cornerRadius),
                end = Offset(x = 0f, y = height),
            ),
            topLeft = Offset(x = cornerRadius, y = height - cornerRadius),
            size = Size(width = width - cornerDiameter, height = cornerRadius),
        )
        // Border.
        drawLine(
            color = currentStyle.headerAndFooterBorderColour,
            start = Offset(x = cornerRadius, y = height),
            end = Offset(x = width - cornerRadius, y = height),
            strokeWidth = BORDER_WIDTH,
        )
    }

    // Left side.
    run {
        // Shadow.
        drawRect(
            brush = Brush.linearGradient(
                colors = shadowColours,
                start = Offset(x = cornerRadius, y = 0f),
                end = Offset.Zero,
            ),
            topLeft = Offset.Zero,
            size = Size(width = cornerRadius, height = height - cornerRadius),
        )
        // Border.
        drawLine(
            color = currentStyle.headerAndFooterBorderColour,
            start = Offset.Zero,
            end = Offset(x = 0f, y = height - cornerRadius),
            strokeWidth = BORDER_WIDTH,
        )
    }

    // Right side.
    run {
        // Shadow.
        drawRect(
            brush = Brush.linearGradient(
                colors = shadowColours,
                start = Offset(x = width - cornerRadius, y = 0f),
                end = Offset(x = width, y = 0f),
            ),
            topLeft = Offset(x = width - cornerRadius, y = 0f),
            size = Size(width = cornerRadius, height = height - cornerRadius),
        )
        // Border.
        drawLine(
            color = currentStyle.headerAndFooterBorderColour,
            start = Offset(x = width, y = 0f),
            end = Offset(x = width, y = height - cornerRadius),
            strokeWidth = BORDER_WIDTH,
        )
    }
}
