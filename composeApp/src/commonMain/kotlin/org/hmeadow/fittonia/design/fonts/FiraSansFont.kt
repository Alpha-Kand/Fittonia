package org.hmeadow.fittonia.design.fonts

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.fira_sans_medium
import org.jetbrains.compose.resources.Font

val firaSansFont: FontFamily
    @Composable
    get() = FontFamily(Font(resource = Res.font.fira_sans_medium))

val headerStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 25.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 500),
        fontFamily = firaSansFont,
    )

val headingLStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 500),
        fontFamily = firaSansFont,
    )

val headingMStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 23.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 450),
        fontFamily = firaSansFont,
    )

val headingSStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 20.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 500),
        fontFamily = firaSansFont,
    )

val paragraphTextStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 400),
        fontFamily = firaSansFont,
    )

val paragraphSpanStyle: SpanStyle
    @Composable
    get() = SpanStyle(
        fontSize = 17.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 400),
        fontFamily = firaSansFont,
    )

val psstStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 14.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.1f).sp,
        fontWeight = FontWeight(weight = 300),
        fontFamily = firaSansFont,
    )

val paragraphParagraphStyle = ParagraphStyle(lineHeight = 23.sp)

val inputLabelStyle: TextStyle
    @Composable
    get() = TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight(weight = 500),
        fontStyle = FontStyle.Italic,
        fontFamily = firaSansFont,
    )

val emoticonStyle = TextStyle(
    fontSize = 23.sp,
    lineHeight = 33.sp,
)
