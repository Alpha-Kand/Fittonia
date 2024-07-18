package org.hmeadow.fittonia.design.fonts

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.hmeadow.fittonia.R

private val questrialFont = FontFamily(
    Font(
        resId = R.font.questrial_regular,
        style = FontStyle.Normal,
    ),
)

val readOnlyFieldTextStyle = TextStyle(
    fontSize = 20.sp,
    lineHeight = 35.sp,
    letterSpacing = (-0.1f).sp,
    fontWeight = FontWeight(weight = 400),
    fontFamily = questrialFont,
)

val readOnlyFieldLightTextStyle = TextStyle(
    fontSize = 15.sp,
    lineHeight = 25.sp,
    letterSpacing = (-0.1f).sp,
    fontWeight = FontWeight(weight = 100),
    fontFamily = questrialFont,
)
