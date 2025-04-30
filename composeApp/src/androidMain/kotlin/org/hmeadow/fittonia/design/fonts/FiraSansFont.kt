package org.hmeadow.fittonia.design.fonts

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.hmeadow.fittonia.R

private val firaSansFont = FontFamily(
    Font(
        resId = R.font.fira_sans_medium,
        style = FontStyle.Normal,
    ),
)

val headerStyle = TextStyle(
    fontSize = 25.sp,
    lineHeight = 33.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 500),
    fontFamily = firaSansFont,
)

val headingLStyle = TextStyle(
    fontSize = 30.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 500),
    fontFamily = firaSansFont,
)

val headingMStyle = TextStyle(
    fontSize = 23.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 450),
    fontFamily = firaSansFont,
)

val headingSStyle = TextStyle(
    fontSize = 20.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 500),
    fontFamily = firaSansFont,
)

val paragraphStyle = TextStyle(
    fontSize = 17.sp,
    lineHeight = 23.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 400),
    fontFamily = firaSansFont,
)

val readOnlyStyle = TextStyle(
    fontSize = 17.sp,
    lineHeight = 23.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 200),
    fontFamily = firaSansFont,
)

val readOnlyLightStyle = TextStyle(
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 100),
    fontFamily = firaSansFont,
)

val psstStyle = TextStyle(
    fontSize = 14.sp,
    lineHeight = 23.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 300),
    fontFamily = firaSansFont,
)

val inputLabelStyle = TextStyle(
    fontSize = 17.sp,
    lineHeight = 23.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 500),
    fontStyle = FontStyle.Italic,
    fontFamily = firaSansFont,
)

val inputHintStyle = TextStyle(
    fontSize = 17.sp,
    lineHeight = 23.sp,
    letterSpacing = (-0.2f).sp,
    fontWeight = FontWeight(weight = 400),
    fontFamily = firaSansFont,
)
