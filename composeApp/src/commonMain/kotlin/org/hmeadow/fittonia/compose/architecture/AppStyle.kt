package org.hmeadow.fittonia.compose.architecture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.compose.components.FittoniaButtonType

sealed interface AppStyle {
    val statusBarColour: Color
    val headerBackgroundColour: Color
    val footerBackgroundColour: Color
    val readOnlyBorderColour : Color
    val readOnlyBackgroundColour: Color
    val readOnlyClearIconColour : Color
    val primaryButtonType: FittoniaButtonType
    val secondaryButtonType: FittoniaButtonType

    @Composable
    fun Background(modifier: Modifier = Modifier)
}

val currentStyle: AppStyle = Empty

data object FittoniaClassic : AppStyle {
    private val backgroundLayer0Colour = Color(color = 0xFF77AA77)
    private val backgroundLayer1Colour = Color(color = 0xFF88AA88)
    private val backgroundLayer2Colour = Color(color = 0xFF99AA99)

    override val statusBarColour = Color(color = 0xAA448844)
    override val headerBackgroundColour = Color(color = 0xAA448844)
    override val footerBackgroundColour = Color(color = 0xAA448844)

    override val readOnlyBorderColour = Color(color = 0xFF446644)
    override val readOnlyBackgroundColour = Color(color = 0xFFDDFFEE)
    override val readOnlyClearIconColour = Color(color = 0xFF222222)

    override val primaryButtonType = FittoniaButtonType(
        borderColour = Color(color = 0xFF550022),
        contentColour = Color(color = 0xFFFFCCFF),
        backgroundColor = Color(color = 0xFF992266),
        disabledContentColor = Color(color = 0xFFFFCCFF),
        disabledBorderColour = Color(color = 0xFF550022).copy(alpha = 0.00f),
        disabledBackgroundColor = Color(color = 0xFFDDAADD),
    )

    override val secondaryButtonType = FittoniaButtonType(
        borderColour = Color(color = 0xFF550022),
        contentColour = Color(color = 0xFF331133),
        backgroundColor = Color(color = 0xFFFFDDFF),
        disabledContentColor = Color(color = 0xFF331133).copy(alpha = 0.5f),
        disabledBorderColour = Color(color = 0xFF550022).copy(alpha = 0.00f),
        disabledBackgroundColor = Color(color = 0xFFEECCEE),
    )

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier = modifier.background(backgroundLayer0Colour)) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.BottomEnd)
                    .padding(top = 100.dp, start = 100.dp)
                    .clip(RoundedCornerShape(topStart = 500.dp))
                    .fillMaxSize()
                    .background(backgroundLayer1Colour),
            ) {}
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.BottomEnd)
                    .padding(top = 200.dp, start = 200.dp)
                    .clip(RoundedCornerShape(topStart = 500.dp))
                    .fillMaxSize()
                    .background(backgroundLayer2Colour),
            ) {}
        }
    }
}

data object Empty : AppStyle {
    override val statusBarColour = Color(color = 0xFFCCCCCC)
    override val headerBackgroundColour = Color(color = 0xFFDDDDDD)
    override val footerBackgroundColour = Color(color = 0xFFDDDDDD)

    override val readOnlyBorderColour = Color(color = 0xFF000000)
    override val readOnlyBackgroundColour = Color(color = 0xFFEEEEEE)
    override val readOnlyClearIconColour = Color(color = 0xFF000000)

    override val primaryButtonType = FittoniaButtonType(
        borderColour = Color(color = 0xFF000000),
        contentColour = Color(color = 0xFF000000),
        backgroundColor = Color(color = 0xFFFFFFFF),
        disabledContentColor = Color(color = 0xFF000000).copy(alpha = 0.5f),
        disabledBorderColour = Color(color = 0xFF000000).copy(alpha = 0.00f),
        disabledBackgroundColor = Color(color = 0xFFFFFFFF),
    )

    override val secondaryButtonType = FittoniaButtonType(
        borderColour = Color(color = 0xFF000000),
        contentColour = Color(color = 0xFF000000),
        backgroundColor = Color(color = 0xFFFFFFFF),
        disabledContentColor = Color(color = 0xFF000000).copy(alpha = 0.5f),
        disabledBorderColour = Color(color = 0xFF000000).copy(alpha = 0.00f),
        disabledBackgroundColor = Color(color = 0xFFFFFFFF),
    )

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier = modifier.background(Color(color = 0x00000000))) {}
    }
}
