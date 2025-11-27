package org.hmeadow.fittonia.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.hmeadow.fittonia.design.fonts.firaSansFont

data object FittoniaButtonConstants {
    const val BORDER_WIDTH = 2
    const val BUTTON_ICON_SIZE = 17
    const val CORNER_RADIUS = 5

    val buttonTextStyle: TextStyle
        @Composable
        get() = TextStyle(
            fontSize = 17.sp,
            lineHeight = 23.sp,
            letterSpacing = (0.08f).sp,
            fontWeight = FontWeight(weight = 600),
            fontFamily = firaSansFont,
        )
}
