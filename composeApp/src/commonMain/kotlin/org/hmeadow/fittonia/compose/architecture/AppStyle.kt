package org.hmeadow.fittonia.compose.architecture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    val readOnlyBorderColour: Color
    val readOnlyBackgroundColour: Color
    val readOnlyClearIconColour: Color
    val primaryButtonType: FittoniaButtonType
    val secondaryButtonType: FittoniaButtonType

    @Composable
    fun Background(modifier: Modifier)
}

val currentStyle: AppStyle = Debug
var appStyleResetHeader by mutableStateOf<ULong>(0u)
var appStyleResetStatusBar by mutableStateOf<ULong>(0u)
var appStyleResetStatusFooter by mutableStateOf<ULong>(0u)

var appStyleResetButton by mutableStateOf<ULong>(0u)
var appStyleResetBackground by mutableStateOf<ULong>(0u)

data object Debug : AppStyle {
    var statusBarColourEdit by mutableStateOf(Color(color = 0xFFCCCCCC))
    var headerBackgroundColourEdit by mutableStateOf(Color(color = 0xFFDDDDDD))
    var footerBackgroundColourEdit by mutableStateOf(Color(color = 0xFFDDDDDD))
    override val statusBarColour: Color
        get() = statusBarColourEdit
    override val headerBackgroundColour: Color
        get() = headerBackgroundColourEdit
    override val footerBackgroundColour: Color
        get() = footerBackgroundColourEdit

    var readOnlyBorderColourEdit by mutableStateOf(Color(color = 0xFF000000))
    var readOnlyBackgroundColourEdit by mutableStateOf(Color(color = 0xFFEEEEEE))
    var readOnlyClearIconColourEdit by mutableStateOf(Color(color = 0xFF000000))
    override val readOnlyBorderColour: Color
        get() = readOnlyBorderColourEdit
    override val readOnlyBackgroundColour: Color
        get() = readOnlyBackgroundColourEdit
    override val readOnlyClearIconColour: Color
        get() = readOnlyClearIconColourEdit

    var primaryButtonBorderColour by mutableStateOf(Color(color = 0xFF000000))
    var primaryButtonContentColour by mutableStateOf(Color(color = 0xFF000000))
    var primaryButtonBackgroundColour by mutableStateOf(Color(color = 0xFFFFFFFF))
    var primaryButtonDisabledBorderColour by mutableStateOf(Color(color = 0xFF000000).copy(alpha = 0.00f))
    var primaryButtonDisabledContentColour by mutableStateOf(Color(color = 0xFF000000).copy(alpha = 0.5f))
    var primaryButtonDisabledBackgroundColour by mutableStateOf(Color(color = 0xFFFFFFFF))

    override val primaryButtonType: FittoniaButtonType
        get() = FittoniaButtonType(
            borderColour = primaryButtonBorderColour,
            contentColour = primaryButtonContentColour,
            backgroundColor = primaryButtonBackgroundColour,
            disabledBorderColour = primaryButtonDisabledBorderColour,
            disabledContentColor = primaryButtonDisabledContentColour,
            disabledBackgroundColor = primaryButtonDisabledBackgroundColour,
        )

    var secondaryButtonBorderColour by mutableStateOf(Color(color = 0xFF000000))
    var secondaryButtonContentColour by mutableStateOf(Color(color = 0xFF000000))
    var secondaryButtonBackgroundColour by mutableStateOf(Color(color = 0xFFFFFFFF))
    var secondaryButtonDisabledBorderColour by mutableStateOf(Color(color = 0xFF000000).copy(alpha = 0.00f))
    var secondaryButtonDisabledContentColour by mutableStateOf(Color(color = 0xFF000000).copy(alpha = 0.5f))
    var secondaryButtonDisabledBackgroundColour by mutableStateOf(Color(color = 0xFFFFFFFF))

    override val secondaryButtonType: FittoniaButtonType
        get() = FittoniaButtonType(
            borderColour = secondaryButtonBorderColour,
            contentColour = secondaryButtonContentColour,
            backgroundColor = secondaryButtonBackgroundColour,
            disabledBorderColour = secondaryButtonDisabledBorderColour,
            disabledContentColor = secondaryButtonDisabledContentColour,
            disabledBackgroundColor = secondaryButtonDisabledBackgroundColour,
        )

    var backgroundColourEdit by mutableStateOf(Color(color = 0xFFFFFFFF))

    @Composable
    override fun Background(modifier: Modifier) {
        key(appStyleResetBackground) {
            Box(modifier = modifier.background(backgroundColourEdit).fillMaxSize()) {}
        }
    }
}

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
        disabledBorderColour = Color(color = 0xFF550022).copy(alpha = 0.00f),
        disabledContentColor = Color(color = 0xFFFFCCFF),
        disabledBackgroundColor = Color(color = 0xFFDDAADD),
    )

    override val secondaryButtonType = FittoniaButtonType(
        borderColour = Color(color = 0xFF550022),
        contentColour = Color(color = 0xFF331133),
        backgroundColor = Color(color = 0xFFFFDDFF),
        disabledBorderColour = Color(color = 0xFF550022).copy(alpha = 0.00f),
        disabledContentColor = Color(color = 0xFF331133).copy(alpha = 0.5f),
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
        disabledBorderColour = Color(color = 0xFF000000).copy(alpha = 0.00f),
        disabledContentColor = Color(color = 0xFF000000).copy(alpha = 0.5f),
        disabledBackgroundColor = Color(color = 0xFFFFFFFF),
    )

    override val secondaryButtonType = FittoniaButtonType(
        borderColour = Color(color = 0xFF000000),
        contentColour = Color(color = 0xFF000000),
        backgroundColor = Color(color = 0xFFFFFFFF),
        disabledBorderColour = Color(color = 0xFF000000).copy(alpha = 0.00f),
        disabledContentColor = Color(color = 0xFF000000).copy(alpha = 0.5f),
        disabledBackgroundColor = Color(color = 0xFFFFFFFF),
    )

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier = modifier.background(Color(color = 0x00000000))) {}
    }
}
