package org.hmeadow.fittonia.screens.debugScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.compose.architecture.Debug
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.appStyleResetBackground
import org.hmeadow.fittonia.compose.architecture.appStyleResetButton
import org.hmeadow.fittonia.compose.architecture.appStyleResetHeader
import org.hmeadow.fittonia.compose.architecture.appStyleResetStatusBar
import org.hmeadow.fittonia.compose.architecture.appStyleResetStatusFooter
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingMStyle

@Composable
fun DebugScreenPaintJobTab(
    footerHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "App's Paint Job",
                style = headingLStyle,
            )
            FittoniaSpacerWidth(width = 5)
            FittoniaIcon(
                modifier = Modifier.requiredSize(40.dp),
                drawableRes = R.drawable.ic_paint,
            )
        }

        FittoniaSpacerHeight(height = 35)

        ColourSliderGroup(
            title = "Status Bar (Top of screen)",
            color = Debug.statusBarColourEdit,
            onUpdate = {
                Debug.statusBarColourEdit = it
                appStyleResetStatusBar = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Header Background",
            color = Debug.headerBackgroundColourEdit,
            onUpdate = {
                Debug.headerBackgroundColourEdit = it
                appStyleResetHeader = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Footer Background",
            color = Debug.footerBackgroundColourEdit,
            onUpdate = {
                Debug.footerBackgroundColourEdit = it
                appStyleResetStatusFooter = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "App Background",
            color = Debug.backgroundColourEdit,
            onUpdate = {
                Debug.backgroundColourEdit = it
                appStyleResetBackground = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Primary Button Border",
            color = Debug.primaryButtonBorderColour,
            onUpdate = {
                Debug.primaryButtonBorderColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Primary Button Content",
            color = Debug.primaryButtonContentColour,
            onUpdate = {
                Debug.primaryButtonContentColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Primary Button Background",
            color = Debug.primaryButtonBackgroundColour,
            onUpdate = {
                Debug.primaryButtonBackgroundColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Border",
            color = Debug.secondaryButtonBorderColour,
            onUpdate = {
                Debug.secondaryButtonBorderColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Content",
            color = Debug.secondaryButtonContentColour,
            onUpdate = {
                Debug.secondaryButtonContentColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Background",
            color = Debug.secondaryButtonBackgroundColour,
            onUpdate = {
                Debug.secondaryButtonBackgroundColour = it
                appStyleResetButton = it.value
            },
        )

        FittoniaSpacerHeight(footerHeight)
    }
}

@Composable
fun ColourSliderGroup(title: String, color: Color, onUpdate: (Color) -> Unit) {
    Column {
        Row {
            Text(
                text = title,
                style = headingMStyle,
                modifier = Modifier.padding(end = 10.dp),
            )
            Box(
                modifier = Modifier
                    .requiredSize(30.dp)
                    .background(color),
            ) {}
        }
        ColourSlider(
            text = "Red",
            sliderPosition = color.red,
            onUpdate = { newRed -> onUpdate(Color(newRed, color.green, color.blue)) },
        )
        ColourSlider(
            text = "Green",
            sliderPosition = color.green,
            onUpdate = { newGreen -> onUpdate(Color(color.red, newGreen, color.blue)) },
        )
        ColourSlider(
            text = "Blue",
            sliderPosition = color.blue,
            onUpdate = { newBlue -> onUpdate(Color(color.red, color.green, newBlue)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColourSlider(text: String, sliderPosition: Float, onUpdate: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        Text(
            modifier = Modifier.requiredWidth(40.dp),
            text = text,
        )
        Slider(
            modifier = Modifier.padding(end = 30.dp),
            value = sliderPosition,
            valueRange = 0f..1f,
            onValueChange = onUpdate,
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    thumbSize = DpSize(width = 20.dp, height = 20.dp),
                )
            },
        )
        Text(modifier = Modifier.requiredWidth(30.dp), text = (255 * sliderPosition).fastRoundToInt().toString())
    }
}
