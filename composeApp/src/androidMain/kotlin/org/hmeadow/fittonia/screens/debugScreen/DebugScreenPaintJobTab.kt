package org.hmeadow.fittonia.screens.debugScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import androidx.core.graphics.toColorInt
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.ReadOnlyEntries
import org.hmeadow.fittonia.compose.architecture.DebugAppStyle
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.appStyleResetBackground
import org.hmeadow.fittonia.compose.architecture.appStyleResetButton
import org.hmeadow.fittonia.compose.architecture.appStyleResetHeader
import org.hmeadow.fittonia.compose.architecture.appStyleResetStatusFooter
import org.hmeadow.fittonia.compose.architecture.appStyleResetTextInput
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingMStyle

enum class ColourGroup {
    STRUCTURE,
    BUTTONS,
    INPUT_FIELDS,
    READ_ONLY,
    TEXT,
}

@Composable
internal fun DebugScreenPaintJobTab(
    onResetColours: () -> Unit,
    footerHeight: Dp,
    viewModel: DebugScreenViewModel,
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

        FittoniaButton(onClick = onResetColours) {
            ButtonText("Reset colours")
        }

        var colourGroup by remember { mutableStateOf(ColourGroup.STRUCTURE) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = colourGroup == ColourGroup.STRUCTURE,
                onClick = { colourGroup = ColourGroup.STRUCTURE },
            )
            Text(text = "Structure", modifier = Modifier.clickable { colourGroup = ColourGroup.STRUCTURE })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = colourGroup == ColourGroup.BUTTONS,
                onClick = { colourGroup = ColourGroup.BUTTONS },
            )
            Text(text = "Buttons", modifier = Modifier.clickable { colourGroup = ColourGroup.BUTTONS })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = colourGroup == ColourGroup.INPUT_FIELDS,
                onClick = { colourGroup = ColourGroup.INPUT_FIELDS },
            )
            Text(text = "Input Fields", modifier = Modifier.clickable { colourGroup = ColourGroup.INPUT_FIELDS })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = colourGroup == ColourGroup.READ_ONLY,
                onClick = { colourGroup = ColourGroup.READ_ONLY },
            )
            Text(text = "Read-Only", modifier = Modifier.clickable { colourGroup = ColourGroup.READ_ONLY })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = colourGroup == ColourGroup.TEXT,
                onClick = { colourGroup = ColourGroup.TEXT },
            )
            Text(text = "Text", modifier = Modifier.clickable { colourGroup = ColourGroup.TEXT })
        }

        Row(modifier = Modifier.padding(horizontal = 50.dp)) {
            Text(text = "<- Blacker")
            FittoniaSpacerWeightRow()
            Text(text = "-----")
            FittoniaSpacerWeightRow()
            Text(text = "Whiter ->")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))

        when (colourGroup) {
            ColourGroup.STRUCTURE -> Structure(viewModel = viewModel)
            ColourGroup.BUTTONS -> Buttons(viewModel = viewModel)
            ColourGroup.INPUT_FIELDS -> InputFields(viewModel = viewModel)
            ColourGroup.READ_ONLY -> ReadOnly(viewModel = viewModel)
            ColourGroup.TEXT -> Text(viewModel = viewModel)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))

        FittoniaSpacerHeight(height = footerHeight)
    }
}

@Composable
private fun ColourSliderGroup(title: String, color: Color, viewModel: DebugScreenViewModel, onUpdate: (Color) -> Unit) {
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
        val colourFlow = remember {
            viewModel.initDebugInputFlow(color.toHex) {
                try {
                    if (it.length == 8) {
                        onUpdate(Color("#$it".toColorInt()))
                    }
                } catch (e: Throwable) {
                    println(e.message)
                }
            }
        }
        val nullString: String? = null
        FittoniaTextInput(inputFlow = colourFlow, label = nullString)
        ColourSlider(
            text = "Red",
            sliderPosition = color.red,
            onUpdate = { newRed ->
                val newColour = Color(alpha = color.alpha, red = newRed, green = color.green, blue = color.blue)
                onUpdate(newColour)
                colourFlow.text = newColour.toHex
            },
        )
        ColourSlider(
            text = "Green",
            sliderPosition = color.green,
            onUpdate = { newGreen ->
                val newColour = Color(alpha = color.alpha, red = color.red, green = newGreen, blue = color.blue)
                onUpdate(newColour)
                colourFlow.text = newColour.toHex
            },
        )
        ColourSlider(
            text = "Blue",
            sliderPosition = color.blue,
            onUpdate = { newBlue ->
                val newColour = Color(alpha = color.alpha, red = color.red, green = color.green, blue = newBlue)
                onUpdate(newColour)
                colourFlow.text = newColour.toHex
            },
        )
    }
}

@Composable
private fun Structure(viewModel: DebugScreenViewModel) {
    Column {
        ColourSliderGroup(
            title = "Header Background",
            color = DebugAppStyle.headerBackgroundColourEdit,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.headerBackgroundColourEdit = it
                appStyleResetHeader = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Footer Background",
            color = DebugAppStyle.footerBackgroundColourEdit,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.footerBackgroundColourEdit = it
                appStyleResetStatusFooter = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Header/Footer Border",
            color = DebugAppStyle.headerAndFooterBorderColourEdit,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.headerAndFooterBorderColourEdit = it
                appStyleResetHeader = it.value
                appStyleResetStatusFooter = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "App Background",
            color = DebugAppStyle.backgroundColourEdit,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.backgroundColourEdit = it
                appStyleResetBackground = it.value
            },
        )
    }
}

@Composable
private fun Buttons(viewModel: DebugScreenViewModel) {
    Column {
        Row {
            FittoniaButton(onClick = {}) { ButtonText("Primary") }
            FittoniaSpacerWidth(10)
            FittoniaButton(
                type = currentStyle.secondaryButtonType,
                onClick = {},
            ) { ButtonText("Secondary") }
        }
        Row {
            FittoniaButton(onClick = {}, enabled = false) { ButtonText("Primary Disabled") }
            FittoniaSpacerWidth(10)
            FittoniaButton(
                enabled = false,
                type = currentStyle.secondaryButtonType,
                onClick = {},
            ) { ButtonText("Secondary Disabled") }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Primary Button Border",
            color = DebugAppStyle.primaryButtonBorderColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.primaryButtonBorderColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Primary Button Content",
            color = DebugAppStyle.primaryButtonContentColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.primaryButtonContentColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Primary Button Background",
            color = DebugAppStyle.primaryButtonBackgroundColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.primaryButtonBackgroundColour = it
                appStyleResetButton = it.value
            },
        )
        ColourSliderGroup(
            title = "Primary Button Border Disabled",
            color = DebugAppStyle.primaryButtonDisabledBorderColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.primaryButtonDisabledBorderColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Primary Button Content Disabled",
            color = DebugAppStyle.primaryButtonDisabledContentColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.primaryButtonDisabledContentColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Primary Button Background Disabled",
            color = DebugAppStyle.primaryButtonDisabledBackgroundColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.primaryButtonDisabledBackgroundColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Border",
            color = DebugAppStyle.secondaryButtonBorderColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.secondaryButtonBorderColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Content",
            color = DebugAppStyle.secondaryButtonContentColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.secondaryButtonContentColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Background",
            color = DebugAppStyle.secondaryButtonBackgroundColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.secondaryButtonBackgroundColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Border Disabled",
            color = DebugAppStyle.secondaryButtonDisabledBorderColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.secondaryButtonDisabledBorderColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Content Disabled",
            color = DebugAppStyle.secondaryButtonDisabledContentColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.secondaryButtonDisabledContentColour = it
                appStyleResetButton = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Secondary Button Background Disabled",
            color = DebugAppStyle.secondaryButtonDisabledBackgroundColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.secondaryButtonDisabledBackgroundColour = it
                appStyleResetButton = it.value
            },
        )
    }
}

@Composable
private fun InputFields(viewModel: DebugScreenViewModel) {
    Column {
        val inputFlow = remember { viewModel.initDebugInputFlow("") }
        FittoniaTextInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            inputFlow = inputFlow,
            hint = "This is hint text",
            label = "Hint Text Field",
        )

        val inputFlow2 = remember { viewModel.initDebugInputFlow("This is input text") }
        FittoniaTextInput(modifier = Modifier.fillMaxWidth(), inputFlow = inputFlow2, label = "Input text field")
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Text Input Border",
            color = DebugAppStyle.textInputBorder,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.textInputBorder = it
                appStyleResetTextInput = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Text Input Background",
            color = DebugAppStyle.textInputBackground,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.textInputBackground = it
                appStyleResetTextInput = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Text Input Content",
            color = DebugAppStyle.textInputContent,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.textInputContent = it
                appStyleResetTextInput = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Text Input Hint",
            color = DebugAppStyle.textInputHint,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.textInputHint = it
                appStyleResetTextInput = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Text Input Hint",
            color = DebugAppStyle.textInputHint,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.textInputHint = it
                appStyleResetTextInput = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Text Input Label",
            color = DebugAppStyle.textInputLabel,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.textInputLabel = it
                appStyleResetTextInput = it.value
            },
        )
    }
}

@Composable
private fun ReadOnly(viewModel: DebugScreenViewModel) {
    Column {
        ReadOnlyEntries(modifier = Modifier.padding(vertical = 20.dp), entries = listOf("Example Read-only field"))
        ColourSliderGroup(
            title = "Read-only field background",
            color = DebugAppStyle.readOnlyBackgroundColourEdit,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.readOnlyBackgroundColourEdit = it
                appStyleResetHeader = it.value
            },
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp))
        ColourSliderGroup(
            title = "Read-only field border",
            color = DebugAppStyle.readOnlyBorderColourEdit,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.readOnlyBorderColourEdit = it
                appStyleResetHeader = it.value
            },
        )
    }
}

@Composable
private fun Text(viewModel: DebugScreenViewModel) {
    Column {
        ColourSliderGroup(
            title = "Header Text",
            color = DebugAppStyle.headerTextColour,
            viewModel = viewModel,
            onUpdate = {
                DebugAppStyle.headerTextColour = it
                appStyleResetHeader = it.value
            },
        )
    }
}

@OptIn(ExperimentalStdlibApi::class)
private val Color.toHex: String
    get() = this.value.toHexString(format = HexFormat.UpperCase).dropLast(8)

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
