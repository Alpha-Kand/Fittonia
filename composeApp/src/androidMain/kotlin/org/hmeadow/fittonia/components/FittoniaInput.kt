package org.hmeadow.fittonia.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.TextFieldDecorator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import org.hmeadow.fittonia.design.fonts.inputLabelStyle

private val inputShape = RoundedCornerShape(corner = CornerSize(5.dp))

class InputFlow(initial: String) : MutableStateFlow<String> by MutableStateFlow(initial)

@Composable
fun FittoniaTextInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )
}

@Composable
fun FittoniaTextInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        label = label,
    )
}

@Composable
fun FittoniaNumberInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
fun FittoniaNumberInput(
    inputFlow: InputFlow,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = label,
    )
}

@Composable
private fun BaseFittoniaInput(
    inputFlow: InputFlow,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    BaseFittoniaInput(
        inputFlow = inputFlow,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BaseFittoniaInput(
    inputFlow: InputFlow,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
) {
    Column {
        label?.let {
            it()
            HMSpacerHeight(height = 7)
        }
        BasicTextField2(
            modifier = modifier,
            value = inputFlow.collectAsState().value,
            onValueChange = { inputFlow.value = it },
            decorator = inputDecorator,
            keyboardOptions = keyboardOptions,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
