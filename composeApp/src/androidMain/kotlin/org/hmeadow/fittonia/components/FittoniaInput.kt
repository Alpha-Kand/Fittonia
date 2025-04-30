package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import inputHintColour
import kotlinx.coroutines.flow.Flow
import org.hmeadow.fittonia.components.FittoniaInputFilter.NO_LETTERS
import org.hmeadow.fittonia.components.FittoniaInputFilter.NO_SYMBOLS
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.design.fonts.inputHintStyle
import org.hmeadow.fittonia.design.fonts.inputLabelStyle

private val inputShape = RoundedCornerShape(corner = CornerSize(5.dp))

class InputFlow(
    val textState: TextFieldState,
    private val onValueChange: (String) -> Unit = {},
) : Flow<String> by snapshotFlow(block = { textState.text.toString().also { onValueChange(it) } }) {
    constructor(initial: String, onValueChange: (String) -> Unit = {}) : this(TextFieldState(initial), onValueChange)

    var text: String
        get() = textState.text.toString()
        set(value) {
            textState.setTextAndPlaceCursorAtEnd(value)
        }

    val isEmpty: Boolean
        get() = textState.text.isEmpty()
}

@Composable
fun FittoniaTextInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    hint: String? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    filters: List<FittoniaInputFilter> = emptyList(),
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        hint = hint,
        modifier = modifier,
        label = label,
        lineLimits = lineLimits,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )
}

@Composable
fun FittoniaTextInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    hint: String? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    filters: List<FittoniaInputFilter> = emptyList(),
    label: (@Composable () -> Unit)? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        hint = hint,
        modifier = modifier,
        lineLimits = lineLimits,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        label = label,
    )
}

@Composable
fun FittoniaNumberInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    hint: String? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    filters: List<FittoniaInputFilter> = emptyList(),
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        hint = hint,
        modifier = modifier,
        label = label,
        lineLimits = lineLimits,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
fun FittoniaNumberInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    hint: String? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    filters: List<FittoniaInputFilter> = emptyList(),
    label: (@Composable () -> Unit)? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        hint = hint,
        modifier = modifier,
        lineLimits = lineLimits,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = label,
    )
}

@Composable
private fun BaseFittoniaInput(
    inputFlow: InputFlow,
    hint: String?,
    keyboardOptions: KeyboardOptions,
    lineLimits: TextFieldLineLimits,
    filters: List<FittoniaInputFilter>,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        hint = hint,
        keyboardOptions = keyboardOptions,
        lineLimits = lineLimits,
        filters = filters,
        label = {
            label?.let {
                Text(
                    text = label,
                    style = inputLabelStyle,
                )
            }
        },
    )
}

@Composable
private fun BaseFittoniaInput(
    inputFlow: InputFlow,
    hint: String?,
    keyboardOptions: KeyboardOptions,
    filters: List<FittoniaInputFilter>,
    lineLimits: TextFieldLineLimits,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    Column {
        label?.let {
            it()
            FittoniaSpacerHeight(height = 7)
        }
        BasicTextField(
            modifier = modifier.onFocusEvent {
                if (it.isFocused) {
                    keyboard?.show()
                }
            },
            state = inputFlow.textState,
            lineLimits = lineLimits,
            inputTransformation = {
                if (!filters.success(input = inputFlow.textState)) {
                    revertAllChanges()
                }
            },
            decorator = { inputField ->
                InputDecorationContent(
                    hint = hint,
                    inputFlow = inputFlow,
                    inputField = inputField,
                )
            },
            keyboardOptions = keyboardOptions,
        )
    }
}

@Composable
private fun InputDecorationContent(
    hint: String?,
    inputFlow: InputFlow,
    inputField: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .requiredHeight(40.dp)
            .border(width = 1.dp, color = Color(0xFF555555), shape = inputShape)
            .background(color = Color(0xFFDDDDDD), shape = inputShape)
            .clip(shape = inputShape)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        hint?.let {
            if (inputFlow.isEmpty) {
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = hint,
                    style = inputHintStyle,
                    color = inputHintColour,
                )
            }
        }
        inputField()
    }
}

enum class FittoniaInputFilter {
    NO_SYMBOLS,
    NO_LETTERS,
}

val socketPortFilters = listOf(NO_SYMBOLS, NO_LETTERS)

private fun List<FittoniaInputFilter>.success(input: TextFieldState): Boolean {
    this.forEach { filter ->
        when (filter) {
            NO_SYMBOLS -> if (!input.text.all { it.isLetterOrDigit() }) return false
            NO_LETTERS -> if (!input.text.all { it.isDigit() }) return false
        }
    }
    return true
}
