package org.hmeadow.fittonia.utility

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.applyIf(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

fun Modifier.borderCyan(): Modifier = then(Modifier.border(1.dp, Color.Cyan))
fun Modifier.borderRed(): Modifier = then(Modifier.border(1.dp, Color.Red))
fun Modifier.borderBlue(): Modifier = then(Modifier.border(1.dp, Color.Blue))
fun Modifier.borderGreen(): Modifier = then(Modifier.border(1.dp, Color.Green))
fun Modifier.borderYellow(): Modifier = then(Modifier.border(1.dp, Color.Yellow))
