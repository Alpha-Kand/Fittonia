package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.utility.isLandscape

@Composable
fun FittoniaModal(
    state: Boolean,
    alignment: Alignment = Center,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
    if (state) {
        Box(
            modifier = Modifier
                .background(color = Color.Black.copy(alpha = 0.0f))
                .fillMaxSize()
                .clickable(onClick = onDismiss),
        ) {
            Box(
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.5f))
                    .fillMaxHeight()
                    .fillMaxWidth(
                        if (isLandscape()) {
                            0.5f
                        } else {
                            0.85f
                        },
                    )
                    .clickable(onClick = onDismiss),
            ) {
                Column(
                    modifier = Modifier
                        .align(alignment = alignment)
                        .padding(horizontal = spacing32)
                        .padding(vertical = 50.dp)
                        .clip(RoundedCornerShape(corner = CornerSize(5.dp)))
                        .background(color = Color.White)
                        .fillMaxWidth()
                        .padding(all = 10.dp),
                    content = {
                        content(onDismiss)
                    },
                )
            }
        }
    }
}

@Composable
fun VerticalLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFAAAAAA))
            .requiredWidth(1.dp)
            .fillMaxHeight(),
    ) {}
}

@Composable
fun HorizontalLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFAAAAAA))
            .requiredHeight(1.dp)
            .fillMaxWidth(),
    ) {}
}

@Composable
fun Footer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        content = content,
    )
}
