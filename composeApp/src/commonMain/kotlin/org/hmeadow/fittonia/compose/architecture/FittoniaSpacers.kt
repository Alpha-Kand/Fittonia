package org.hmeadow.fittonia.compose.architecture

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FittoniaSpacerHeight(height: Int) {
    Spacer(modifier = Modifier.height(height.dp))
}

@Composable
fun FittoniaSpacerHeight(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun FittoniaSpacerWidth(width: Int) {
    Spacer(modifier = Modifier.width(width.dp))
}

@Composable
fun FittoniaSpacerWidth(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun ColumnScope.FittoniaSpacerWeightColumn(weight: Float = 1.0f) {
    Spacer(modifier = Modifier.weight(weight))
}

@Composable
fun RowScope.FittoniaSpacerWeightRow(weight: Float = 1.0f) {
    Spacer(modifier = Modifier.weight(weight))
}
