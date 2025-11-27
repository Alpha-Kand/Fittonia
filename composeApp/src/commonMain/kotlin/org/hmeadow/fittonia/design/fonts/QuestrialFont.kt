package org.hmeadow.fittonia.design.fonts

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.questrial_regular
import org.jetbrains.compose.resources.Font

val questrialFont: FontFamily
    @Composable
    get() = FontFamily(Font(resource = Res.font.questrial_regular))

val readOnlyFieldTextStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 20.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 400),
        fontFamily = questrialFont,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
        baselineShift = BaselineShift(multiplier = 0.4f),
    )

val readOnlyFieldSmallTextStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 15.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 400),
        fontFamily = questrialFont,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
    )

val readOnlyFieldLightTextStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 15.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 100),
        fontFamily = questrialFont,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Top,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
        baselineShift = BaselineShift(multiplier = 0.4f),
    )

@Composable
fun inputInputStyle(color: Color) = TextStyle(
    fontSize = 17.sp,
    lineHeight = 23.sp,
    letterSpacing = (-0.1f).sp,
    fontWeight = FontWeight(weight = 200),
    fontFamily = questrialFont,
    color = color,
)

val inputHintStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 200),
        fontFamily = questrialFont,
    )
