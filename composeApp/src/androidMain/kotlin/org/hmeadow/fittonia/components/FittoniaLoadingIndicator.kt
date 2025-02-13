package org.hmeadow.fittonia.components

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun FittoniaLoadingIndicator(modifier: Modifier = Modifier, colour: Color = Color(0xFFFFCCFF)) {
    CircularProgressIndicator(
        modifier = modifier.requiredSize(17.dp),
        color = colour,
        strokeWidth = 4.dp,
        strokeCap = StrokeCap.Round,
    )
}
