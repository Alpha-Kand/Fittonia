package org.hmeadow.fittonia.compose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.compose.architecture.appStyleResetButton
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButtonConstants.BORDER_WIDTH
import org.hmeadow.fittonia.compose.components.FittoniaButtonConstants.BUTTON_ICON_SIZE
import org.hmeadow.fittonia.compose.components.FittoniaButtonConstants.CORNER_RADIUS
import org.hmeadow.fittonia.compose.components.FittoniaButtonConstants.buttonTextStyle
import org.hmeadow.fittonia.utility.InfoBorderState.handleClicks
import org.hmeadow.fittonia.utility.InfoBorderState.infoBorderActive
import org.hmeadow.fittonia.utility.InfoBorderState.infoBox
import org.hmeadow.fittonia.utility.SuspendedAction
import org.hmeadow.fittonia.utility.infoBorder

data class FittoniaButtonScope(
    private val type: FittoniaButtonType,
    private val enabled: Boolean,
) {

    @Composable
    fun ButtonText(text: String) {
        Text(
            text = text,
            color = if (enabled) {
                type.contentColour
            } else {
                type.disabledContentColor
            },
            style = buttonTextStyle,
        )
    }

    @Composable
    fun ButtonIcon(painter: Painter, size: Dp?) {
        FittoniaIcon(
            modifier = Modifier.requiredSize(size = size ?: BUTTON_ICON_SIZE.dp),
            painter = painter,
            tint = if (enabled) {
                type.contentColour
            } else {
                type.disabledContentColor
            },
        )
    }
}

data class FittoniaButtonType(
    val backgroundColor: Color,
    val disabledBackgroundColor: Color,
    val contentColour: Color,
    val disabledContentColor: Color,
    val borderColour: Color,
    val disabledBorderColour: Color,
)

@Composable
fun FittoniaButton(
    modifier: Modifier = Modifier,
    type: FittoniaButtonType = currentStyle.primaryButtonType,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onInfo: (@Composable () -> Unit)? = null,
    content: @Composable FittoniaButtonScope.() -> Unit,
) {
    val isLoading = if (onClick is SuspendedAction) {
        onClick.isRunning
    } else {
        false
    }
    key(appStyleResetButton) {
        Button(
            modifier = modifier.infoBorder(onInfo = onInfo, verticalPadding = 0f),
            shape = RoundedCornerShape(corner = CornerSize(CORNER_RADIUS.dp)),
            border = BorderStroke(
                width = BORDER_WIDTH.dp,
                color = if (enabled) {
                    type.borderColour
                } else {
                    type.disabledBorderColour
                },
            ),
            enabled = if (infoBorderActive) {
                true
            } else {
                enabled
            },
            onClick = { handleClicks(onClick = onClick, onInfo = { infoBox = onInfo }) },
            content = {
                if (isLoading) {
                    FittoniaLoadingIndicator(colour = type.contentColour)
                } else {
                    FittoniaButtonScope(type = type, enabled = enabled).content()
                }
            },
            colors = buttonColors(
                containerColor = type.backgroundColor,
                contentColor = type.contentColour,
                disabledContainerColor = type.disabledBackgroundColor,
                disabledContentColor = type.disabledContentColor,
            ),
        )
    }
}
