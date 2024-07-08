package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HMSpacerHeight(height: Int) {
    Spacer(modifier = Modifier.height(height.dp))
}

@Composable
fun HMSpacerWidth(width: Int) {
    Spacer(modifier = Modifier.width(width.dp))
}

@Composable
fun ColumnScope.HMSpacerWeightColumn(weight: Float = 1.0f) {
    Spacer(modifier = Modifier.weight(weight))
}

@Composable
fun RowScope.HMSpacerWeightRow(weight: Float = 1.0f) {
    Spacer(modifier = Modifier.weight(weight))
}

private enum class FooEnum {
    HEADER, FOOTER, CONTENT, OVERLAY,
}

@Composable
fun FittoniaBackground(
    content: @Composable BoxScope.(PaddingValues) -> Unit,
    header: (@Composable BoxScope.() -> Unit)? = null,
    footer: (@Composable BoxScope.() -> Unit)? = null,
    overlay: @Composable BoxWithConstraintsScope.(PaddingValues) -> Unit = {},
) {
    Box(modifier = Modifier.background(layer0Colour)) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(top = 100.dp, start = 100.dp)
                .clip(RoundedCornerShape(topStart = 500.dp))
                .fillMaxSize()
                .background(layer1Colour),
        ) {}
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(top = 200.dp, start = 200.dp)
                .clip(RoundedCornerShape(topStart = 500.dp))
                .fillMaxSize()
                .background(layer2Colour),
        ) {}
        SubcomposeLayout(modifier = Modifier) { constraints ->
            val newConstraints = constraints.copy(
                minWidth = 0,
                minHeight = 0,
            )

            val headerPlaceables = subcompose(FooEnum.HEADER) {
                header?.let {
                    Box(
                        modifier = Modifier
                            .requiredHeight(40.dp)
                            .fillMaxWidth()
                            .background(color = Color(0xFF448844)),
                    ) {
                        header()
                    }
                }
            }.map { it.measure(newConstraints) }

            val footerPlaceables = subcompose(FooEnum.FOOTER) {
                footer?.let {
                    Box(
                        modifier = Modifier
                            .background(color = Color(0xFF448844))
                            .fillMaxWidth(),
                    ) {
                        footer()
                    }
                }
            }.map { it.measure(newConstraints) }

            val paddingValues = PaddingValues(
                top = headerPlaceables.maxOfOrNull { it.height }?.toDp() ?: 0.dp,
                bottom = footerPlaceables.maxOfOrNull { it.height }?.toDp() ?: 0.dp,
            )

            val contentPlaceables = subcompose(FooEnum.CONTENT) {
                content(paddingValues)
            }.map { it.measure(newConstraints) }

            val overlayPlaceables = subcompose(FooEnum.OVERLAY) {
                BoxWithConstraints(Modifier.fillMaxSize()) { overlay(paddingValues) }
            }.single().measure(newConstraints)

            layout(constraints.maxWidth, constraints.maxHeight) {
                headerPlaceables.forEach {
                    it.place(x = 0, y = 0, zIndex = 1f)
                }
                contentPlaceables.forEach {
                    it.place(x = 0, y = headerPlaceables.maxOfOrNull { it.height } ?: 0)
                }
                footerPlaceables.forEach {
                    it.place(x = 0, y = constraints.maxHeight - it.height, zIndex = 1f)
                }
                overlayPlaceables.place(x = 0, y = 0, zIndex = 2f)
            }
        }
    }
}

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
fun VerticalLine() {
    Box(
        modifier = Modifier
            .background(Color(0xFFAAAAAA))
            .requiredWidth(1.dp)
            .fillMaxHeight(),
    ) {}
}

@Composable
fun HorizontalLine() {
    Box(
        modifier = Modifier
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
