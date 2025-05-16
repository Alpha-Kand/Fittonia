package org.hmeadow.fittonia.compose.components

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data object FittoniaButtonConstants {
    const val BORDER_WIDTH = 2
    const val BUTTON_ICON_SIZE = 17
    const val CORNER_RADIUS = 5

    val buttonTextStyle = TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = (0.08f).sp,
        fontWeight = FontWeight(weight = 600),
    )
}
