package org.hmeadow.fittonia.utility

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.utility.InfoBorderState.GRADIENT_SIZE
import org.hmeadow.fittonia.utility.InfoBorderState.HORIZONTAL_GRADIENT_PADDING
import org.hmeadow.fittonia.utility.InfoBorderState.LOOP_MILLIS
import org.hmeadow.fittonia.utility.InfoBorderState.VERTICAL_GRADIENT_PADDING
import org.hmeadow.fittonia.utility.InfoBorderState.focusedColours
import org.hmeadow.fittonia.utility.InfoBorderState.infoBorderActive
import org.hmeadow.fittonia.utility.InfoBorderState.infoBox
import org.hmeadow.fittonia.utility.InfoBorderState.unfocusedColours

object InfoBorderState {
    var infoBorderActive by mutableStateOf(value = false)
        private set
    var infoBox by mutableStateOf<(@Composable () -> Unit)?>(value = null)
    const val HORIZONTAL_GRADIENT_PADDING = 7f
    const val VERTICAL_GRADIENT_PADDING = 7f
    const val GRADIENT_SIZE = 100f
    const val LOOP_MILLIS = 1300
    val unfocusedColours = listOf(Color.Green, Color.White)
    val focusedColours = listOf(Color(0xFFFC44A6), Color.Green, Color.Green, Color(0xFFFC44A6))

    fun handleClicks(onClick: () -> Unit, onInfo: (() -> Unit)?) {
        if (infoBorderActive) {
            onInfo?.invoke()
        } else {
            onClick()
        }
    }

    fun enableInfoBorder() {
        infoBorderActive = true
        infoBox = null
    }

    fun clearInfoBorderState() {
        infoBorderActive = false
        infoBox = null
    }

    @Composable
    fun BoxScope.infoBoxOverlay() {
        infoBox?.let {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAAFFFFFF))
                    .clickable(
                        onClick = ::clearInfoBorderState,
                        indication = null,
                        interactionSource = interactionSource,
                    ),
            ) {}
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .background(color = Color.White)
                    .border(2.dp, Color.Black)
                    .align(Alignment.Center)
                    .padding(all = 7.dp)
                    .clickable(
                        onClick = ::clearInfoBorderState,
                        indication = null,
                        interactionSource = interactionSource,
                    ),
            ) {
                it()
            }
        }
    }
}

fun Modifier.infoBorder(
    onInfo: (@Composable () -> Unit)? = null,
    horizontalPadding: Float = HORIZONTAL_GRADIENT_PADDING,
    verticalPadding: Float = VERTICAL_GRADIENT_PADDING,
): Modifier = composed {
    val gradientOffset by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = GRADIENT_SIZE * 2,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = LOOP_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    drawBehind {
        if (infoBorderActive && onInfo != null) {
            drawRect(
                topLeft = Offset(x = -horizontalPadding, y = -verticalPadding),
                size = this.size.copy(
                    width = this.size.width + (2 * horizontalPadding),
                    height = this.size.height + (2 * verticalPadding),
                ),
                brush = Brush.linearGradient(
                    colors = if (infoBox == onInfo) {
                        focusedColours
                    } else {
                        unfocusedColours
                    },
                    start = Offset(x = 0f + gradientOffset, y = 0f),
                    end = Offset(x = GRADIENT_SIZE + gradientOffset, y = GRADIENT_SIZE),
                    tileMode = TileMode.Repeated,
                ),
            )
        }
    }
}
