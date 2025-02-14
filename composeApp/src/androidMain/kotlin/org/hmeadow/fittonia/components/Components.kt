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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FittoniaComingSoon(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        SubcomposeLayout { constraints ->
            val foo = subcompose("foo") {
                content()
            }.single().measure(constraints)

            val comingSoon = subcompose("comingSoon") {
                Text(
                    text = "Coming soon",
                    modifier = Modifier
                        .background(color = Color.Black.copy(alpha = 0.66f))
                        .requiredWidth(foo.measuredWidth.toDp()),
                    color = Color.Yellow,
                    textAlign = TextAlign.Center,
                )
            }.single().measure(constraints)

            layout(foo.width, foo.height) {
                foo.place(x = 0, y = 0)
                comingSoon.place(x = 0, y = foo.height - (foo.height / 2) - (comingSoon.height / 2))
            }
        }
    }
}

@Composable
fun FittoniaModal(
    state: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
    if (state) {
        Box(
            modifier = Modifier
                .background(color = Color.Black.copy(alpha = 0.5f))
                .fillMaxSize()
                .clickable(onClick = onDismiss),
        ) {
            Column(
                modifier = Modifier
                    .align(alignment = Center)
                    .padding(all = 30.dp)
                    .clip(RoundedCornerShape(corner = CornerSize(5.dp)))
                    .background(color = Color.White)
                    .fillMaxWidth(),
                content = {
                    content(onDismiss)
                },
            )
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
