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
import androidx.compose.foundation.text.input.TextFieldDecorator
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
import kotlinx.coroutines.flow.Flow
import org.hmeadow.fittonia.components.FittoniaInputFilter.NO_LETTERS
import org.hmeadow.fittonia.components.FittoniaInputFilter.NO_SYMBOLS
import org.hmeadow.fittonia.design.fonts.inputLabelStyle

private val inputShape = RoundedCornerShape(corner = CornerSize(5.dp))

class InputFlow(
    val textState: TextFieldState,
) : Flow<String> by snapshotFlow(block = { textState.text.toString() }) {
    constructor(initial: String) : this(TextFieldState(initial))

    var text: String
        get() = textState.text.toString()
        set(value) {
            textState.setTextAndPlaceCursorAtEnd(value)
        }
}

@Composable
fun FittoniaTextInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    filters: List<FittoniaInputFilter> = emptyList(),
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        label = label,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )
}

@Composable
fun FittoniaTextInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    filters: List<FittoniaInputFilter> = emptyList(),
    label: (@Composable () -> Unit)? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        label = label,
    )
}

@Composable
fun FittoniaNumberInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    filters: List<FittoniaInputFilter> = emptyList(),
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        label = label,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
fun FittoniaNumberInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    filters: List<FittoniaInputFilter> = emptyList(),
    label: (@Composable () -> Unit)? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = label,
    )
}

@Composable
private fun BaseFittoniaInput(
    inputFlow: InputFlow,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    filters: List<FittoniaInputFilter>,
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
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
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    filters: List<FittoniaInputFilter>,
    label: (@Composable () -> Unit)? = null,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    Column {
        label?.let {
            it()
            HMSpacerHeight(height = 7)
        }
        BasicTextField(
            modifier = modifier.onFocusEvent {
                if (it.isFocused) {
                    keyboard?.show()
                }
            },
            state = inputFlow.value,
            inputTransformation = {
                if (!filters.success(input = inputFlow.value)) {
                    revertAllChanges()
                }
            },
            decorator = inputDecorator,
            keyboardOptions = keyboardOptions,
        )
    }
}

private val inputDecorator = TextFieldDecorator { inputField ->
    Box(
        modifier = Modifier
            .requiredHeight(40.dp)
            .border(width = 1.dp, color = Color(0xFF555555), shape = inputShape)
            .background(color = Color(0xFFDDDDDD), shape = inputShape)
            .clip(inputShape)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
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
