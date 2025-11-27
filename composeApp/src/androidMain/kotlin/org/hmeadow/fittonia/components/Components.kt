package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.compose.components.FittoniaButtonConstants.BORDER_WIDTH
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing64
import org.hmeadow.fittonia.utility.isLandscape

@Composable
fun FittoniaModal(
    state: Boolean,
    alignment: Alignment = Center,
    contentBackgroundColour: Color = Color.White,
    contentBorderColour: Color = Color.Black,
    onDismiss: () -> Unit,
    topContent: @Composable ColumnScope.() -> Unit = {},
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
                    .fillMaxHeight()
                    .fillMaxWidth(
                        if (isLandscape()) {
                            0.5f
                        } else {
                            0.85f
                        },
                    )
                    .clickable(onClick = onDismiss)
                    .drawBehind {
                        drawRect(
                            color = Color.White,
                            size = Size(width = size.width, height = size.height),
                        )
                        drawLine(
                            color = Color.Black,
                            start = Offset(x = size.width, y = 0f),
                            end = Offset(x = size.width, y = size.height),
                            strokeWidth = 4f,
                        )
                    },
            ) {
                Column(
                    modifier = Modifier
                        .align(alignment = alignment)
                        .padding(horizontal = spacing16)
                        .padding(vertical = spacing64),
                ) {
                    topContent()
                    Column(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(corner = CornerSize(5.dp)))
                            .background(
                                color = contentBackgroundColour,
                                shape = RoundedCornerShape(corner = CornerSize(5.dp)),
                            )
                            .border(
                                width = BORDER_WIDTH.dp,
                                color = contentBorderColour,
                                shape = RoundedCornerShape(corner = CornerSize(5.dp)),
                            )
                            .fillMaxWidth()
                            .padding(horizontal = spacing16),
                        content = {
                            content(onDismiss)
                        },
                    )
                }
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
fun HorizontalLine(modifier: Modifier = Modifier, color: Color = Color(color = 0xFFAAAAAA)) {
    Box(
        modifier = modifier
            .background(color = color)
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
