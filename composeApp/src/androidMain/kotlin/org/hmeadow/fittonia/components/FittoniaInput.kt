package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import inputHintColour
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.components.FittoniaInputFilter.NO_LETTERS
import org.hmeadow.fittonia.components.FittoniaInputFilter.NO_SYMBOLS
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.design.fonts.inputHintStyle
import org.hmeadow.fittonia.design.fonts.inputLabelStyle
import org.hmeadow.fittonia.utility.InfoBorderState.infoBorderActive
import org.hmeadow.fittonia.utility.InfoBorderState.infoBox
import org.hmeadow.fittonia.utility.infoBorder
import kotlin.coroutines.CoroutineContext

private val inputShape = RoundedCornerShape(corner = CornerSize(5.dp))

class InputFlow(
    val textState: TextFieldState,
    private val onValueChange: (String) -> Unit = {},
) : Flow<String> by snapshotFlow(block = { textState.text.toString() }), CoroutineScope {
    constructor(initial: String, onValueChange: (String) -> Unit = {}) : this(TextFieldState(initial), onValueChange)

    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        println("InputFlow error: ${throwable.message}") // TODO - handle errors, crashlytics? before release
    }

    init {
        launch {
            while (true) {
                collect { onValueChange(it) }
            }
        }
    }

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
    onInfo: (@Composable () -> Unit)? = null,
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
        onInfo = onInfo,
    )
}

@Composable
fun FittoniaTextInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    hint: String? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    filters: List<FittoniaInputFilter> = emptyList(),
    onInfo: (@Composable () -> Unit)? = null,
    label: (@Composable () -> Unit)? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        hint = hint,
        modifier = modifier,
        lineLimits = lineLimits,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        onInfo = onInfo,
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
    onInfo: (@Composable () -> Unit)? = null,
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
        onInfo = onInfo,
    )
}

@Composable
fun FittoniaNumberInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    hint: String? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    filters: List<FittoniaInputFilter> = emptyList(),
    onInfo: (@Composable () -> Unit)? = null,
    label: (@Composable () -> Unit)? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        hint = hint,
        modifier = modifier,
        lineLimits = lineLimits,
        filters = filters,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        onInfo = onInfo,
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
    onInfo: (@Composable () -> Unit)?,
    label: String?,
    modifier: Modifier = Modifier,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        hint = hint,
        keyboardOptions = keyboardOptions,
        lineLimits = lineLimits,
        filters = filters,
        onInfo = onInfo,
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
    onInfo: (@Composable () -> Unit)?,
    label: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value && onInfo != null && infoBorderActive) {
        infoBox = onInfo
    }
    Column {
        label?.let {
            it()
            FittoniaSpacerHeight(height = 7)
        }
        BasicTextField(
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusEvent {
                    if (infoBorderActive) {
                        focusRequester.freeFocus()
                    } else if (it.isFocused) {
                        keyboard?.show()
                    }
                }
                .infoBorder(onInfo = onInfo),
            interactionSource = interactionSource,
            readOnly = infoBorderActive,
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
