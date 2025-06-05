package org.hmeadow.fittonia.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.fonts.psstStyle

data class CheckboxColours(
    val checkedColour: Color,
    val uncheckedColour: Color,
    val checkmarkColour: Color,
)

@Composable
fun FittoniaCheckbox(
    initialState: Boolean = false,
    label: String = "",
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit,
) {
    var checkedState by remember { mutableStateOf(initialState) }
    Row(
        modifier = modifier.clickable {
            checkedState = !checkedState
            onToggle(checkedState)
        },
        verticalAlignment = CenterVertically,
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = currentStyle.secondaryButtonType.backgroundColor,
                uncheckedColor = currentStyle.secondaryButtonType.backgroundColor,
                checkmarkColor = currentStyle.secondaryButtonType.contentColour,
            ),
        )
        FittoniaSpacerWidth(width = spacing4)
        Text(
            text = label,
            style = psstStyle,
        )
    }
}

