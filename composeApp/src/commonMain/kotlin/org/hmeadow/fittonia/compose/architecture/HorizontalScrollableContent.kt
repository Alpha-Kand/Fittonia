package org.hmeadow.fittonia.compose.architecture

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout

@Composable
fun HorizontalScrollableContent(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    endContent: @Composable () -> Unit,
) {
    Column { // This is needed so that this component takes up space in a container.
        SubcomposeLayout(modifier = modifier) { constraints ->
            val endContentPlaceable: Placeable = subcompose(slotId = "ID_1") {
                endContent()
            }.single().measure(constraints = constraints)
            val contentPlaceable: Placeable = subcompose(slotId = "ID_2") {
                Box(
                    modifier = Modifier
                        .requiredWidth(width = (constraints.maxWidth - endContentPlaceable.measuredWidth).toDp())
                        .horizontalScroll(state = rememberScrollState()),
                ) {
                    content(this)
                }
            }.single().measure(constraints = constraints)
            layout(constraints.maxWidth, constraints.maxHeight) {
                contentPlaceable.place(x = -(endContentPlaceable.measuredWidth / 2), y = 0)
                endContentPlaceable.place(x = constraints.maxWidth / 2 - (endContentPlaceable.measuredWidth / 2), y = 0)
            }
        }
    }
}
