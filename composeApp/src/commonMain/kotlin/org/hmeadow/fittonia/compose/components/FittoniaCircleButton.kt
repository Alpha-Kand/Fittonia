package org.hmeadow.fittonia.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButtonConstants.BORDER_WIDTH
import org.hmeadow.fittonia.compose.components.FittoniaCircleButtonConstants.REQUIRED_SIZE
import org.hmeadow.fittonia.compose.components.FittoniaCircleButtonConstants.RIPPLE_SIZE

@Composable
fun FittoniaCircleButton(
    type: FittoniaButtonType = currentStyle.primaryButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable FittoniaButtonScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .padding(5.dp)
            .requiredSize(REQUIRED_SIZE.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(radius = RIPPLE_SIZE.dp),
                onClick = onClick,
            )
            .background(
                color = type.backgroundColor,
                shape = RoundedCornerShape(REQUIRED_SIZE.dp),
            )
            .border(width = BORDER_WIDTH.dp, color = type.borderColour, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        FittoniaButtonScope(type = type, enabled = enabled).content()
    }
}
