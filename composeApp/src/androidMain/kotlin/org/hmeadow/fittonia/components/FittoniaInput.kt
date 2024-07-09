package org.hmeadow.fittonia.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.TextFieldDecorator
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FittoniaTextInput(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState = rememberTextFieldState(initialText = ""),
) {
    FittoniaInput(
        modifier = modifier,
        textFieldState = textFieldState,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FittoniaNumberInput(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState = rememberTextFieldState(initialText = ""),
) {
    FittoniaInput(
        modifier = modifier,
        textFieldState = textFieldState,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FittoniaInput(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    keyboardOptions: KeyboardOptions,
) {
    BasicTextField2(
        modifier = modifier,
        state = textFieldState,
        decorator = inputDecorator,
        keyboardOptions = keyboardOptions,
    )
}

@OptIn(ExperimentalFoundationApi::class)
val inputDecorator = TextFieldDecorator { inputField ->
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
