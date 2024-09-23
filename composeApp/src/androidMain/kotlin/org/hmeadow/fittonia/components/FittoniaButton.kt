package org.hmeadow.fittonia.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.utility.SuspendedAction

@Composable
@Preview
private fun Preview() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Primary")
        HMSpacerHeight(height = 4)
        FittoniaButton(onClick = { /*TODO*/ }) {
            ButtonText(text = "Hello There!")
            HMSpacerWidth(width = 5)
            ButtonIcon(drawableRes = R.drawable.ic_add)
        }

        HMSpacerHeight(height = 16)

        Text(text = "Secondary")
        HMSpacerHeight(height = 4)
        FittoniaButton(
            type = FittoniaButtonType.Secondary,
            onClick = { /*TODO*/ },
        ) {
            ButtonText(text = "Hello There!")
        }

        HMSpacerHeight(height = 16)

        Text(text = "Primary Disabled")
        HMSpacerHeight(height = 4)
        FittoniaButton(
            type = FittoniaButtonType.Primary,
            enabled = false,
            onClick = { /*TODO*/ },
        ) {
            ButtonText(text = "Hello There!")
        }

        HMSpacerHeight(height = 16)

        Text(text = "Secondary Disabled")
        HMSpacerHeight(height = 4)
        FittoniaButton(
            type = FittoniaButtonType.Secondary,
            enabled = false,
            onClick = { /*TODO*/ },
        ) {
            ButtonText(text = "Hello There!")
        }
    }
}

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
            style = TextStyle(
                fontSize = 17.sp,
                lineHeight = 23.sp,
                letterSpacing = (0.08f).sp,
                fontWeight = FontWeight(weight = 600),
            ),
        )
    }

    @Composable
    fun ButtonIcon(@DrawableRes drawableRes: Int) {
        FittoniaIcon(
            modifier = Modifier.requiredSize(17.dp),
            drawableRes = drawableRes,
            tint = if (enabled) {
                type.contentColour
            } else {
                type.disabledContentColor
            },
        )
    }
}

sealed interface FittoniaButtonType {
    val backgroundColor: Color
    val disabledBackgroundColor: Color
    val contentColour: Color
    val disabledContentColor: Color
    val borderColour: Color
    val disabledBorderColour: Color

    data object Primary : FittoniaButtonType {
        override val backgroundColor: Color = Color(0xFF992266)
        override val disabledBackgroundColor: Color = Color(0xFFDDAADD)
        override val contentColour: Color = Color(0xFFFFCCFF)
        override val disabledContentColor: Color = contentColour
        override val borderColour: Color = Color(0xFF550022)
        override val disabledBorderColour: Color = borderColour.copy(alpha = 0.00f)
    }

    data object Secondary : FittoniaButtonType {
        override val backgroundColor: Color = Color(0xFFFFDDFF)
        override val disabledBackgroundColor: Color = Color(0xFFEECCEE)
        override val contentColour: Color = Color(0xFF331133)
        override val disabledContentColor: Color = contentColour.copy(alpha = 0.5f)
        override val borderColour: Color = Color(0xFF550022)
        override val disabledBorderColour: Color = borderColour.copy(alpha = 0.00f)
    }
}

@Composable
fun FittoniaButton(
    modifier: Modifier = Modifier,
    type: FittoniaButtonType = FittoniaButtonType.Primary,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable FittoniaButtonScope.() -> Unit,
) {
    val isLoading = if (onClick is SuspendedAction) {
        onClick.isRunning
    } else {
        false
    }
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(corner = CornerSize(5.dp)),
        border = BorderStroke(
            width = 2.dp,
            color = if (enabled) {
                type.borderColour
            } else {
                type.disabledBorderColour
            },
        ),
        enabled = enabled,
        onClick = onClick,
        content = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.requiredSize(17.dp),
                    color = type.contentColour,
                    strokeWidth = 4.dp,
                    strokeCap = StrokeCap.Round,
                )
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
