package org.hmeadow.fittonia.compose.components

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
import androidx.compose.runtime.key
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.appStyleResetTextInput
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaInputFilter.NO_LETTERS
import org.hmeadow.fittonia.compose.components.FittoniaInputFilter.NO_SYMBOLS
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.inputHintStyle
import org.hmeadow.fittonia.design.fonts.inputInputStyle
import org.hmeadow.fittonia.design.fonts.inputLabelStyle
import org.hmeadow.fittonia.utility.InfoBorderState.infoBorderActive
import org.hmeadow.fittonia.utility.InfoBorderState.infoBox
import org.hmeadow.fittonia.utility.debug
import org.hmeadow.fittonia.utility.infoBorder
import recordThrowable
import kotlin.coroutines.CoroutineContext

private val inputShape = RoundedCornerShape(corner = CornerSize(5.dp))

/**
 * Launching the flow collection coroutines in the [InputFlow] constructors can lead to a race condition if those
 * coroutines reference other [InputFlow]s that haven't been initialized yet. The intent here is to prepare all the
 * [InputFlow]s coroutines and only launch them when all [InputFlow]s are ready.
 */
class InputFlowCollectionLauncher() {
    private val flowCollectionCallbacks = mutableListOf<() -> Unit>()
    var hasLaunched = false
        private set
    val hasCallbacks: Boolean
        get() = flowCollectionCallbacks.isNotEmpty()

    fun add(block: () -> Unit) {
        flowCollectionCallbacks.add(block)
    }

    fun launch() {
        hasLaunched = true
        flowCollectionCallbacks.forEach {
            it()
        }
    }
}

class InputFlow(
    val textState: TextFieldState,
    private val onValueChange: ((String) -> Unit)? = null,
    launcher: InputFlowCollectionLauncher,
) : Flow<String> by snapshotFlow(block = { textState.text.toString() }), CoroutineScope {

    constructor(
        initial: String,
        onValueChange: ((String) -> Unit)? = null,
        launcher: InputFlowCollectionLauncher,
    ) : this(
        textState = TextFieldState(initial),
        onValueChange = onValueChange,
        launcher = launcher,
    )

    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        recordThrowable(throwable = throwable)
        debug {
            println("InputFlow error: ${throwable.message}")
        }
    }

    init {
        onValueChange?.let {
            launcher.add {
                launch {
                    while (isActive) {
                        collect { onValueChange(it) }
                    }
                }
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

data class TextInputColours(
    val border: Color,
    val background: Color,
    val content: Color,
    val hint: Color,
    val label: Color,
)

@Composable
fun FittoniaTextInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    hint: String? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    filters: List<FittoniaInputFilter> = emptyList(),
    onInfo: (@Composable () -> Unit)? = null,
) {
    val noLabel: String? = null
    BaseFittoniaInput(
        inputFlow = inputFlow,
        hint = hint,
        modifier = modifier,
        label = noLabel,
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
                    color = currentStyle.textInputColours.label,
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
    key(appStyleResetTextInput) {
        Column {
            label?.let {
                it()
                FittoniaSpacerHeight(height = spacing4)
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
                textStyle = inputInputStyle(color = currentStyle.textInputColours.content),
            )
        }
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
            .requiredHeight(height = spacing32)
            .border(width = 1.dp, color = currentStyle.textInputColours.border, shape = inputShape)
            .background(color = currentStyle.textInputColours.background, shape = inputShape)
            .clip(shape = inputShape)
            .padding(horizontal = spacing8),
        contentAlignment = Alignment.CenterStart,
    ) {
        hint?.let {
            if (inputFlow.isEmpty) {
                Text(
                    text = hint,
                    style = inputHintStyle,
                    color = currentStyle.textInputColours.hint,
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
