package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import backgroundLayer0Colour
import backgroundLayer1Colour
import backgroundLayer2Colour
import org.hmeadow.fittonia.MainActivity.Companion.imeHeight
import org.hmeadow.fittonia.MainActivity.Companion.navBarHeight
import org.hmeadow.fittonia.MainActivity.Companion.statusBarsHeight
import org.hmeadow.fittonia.utility.applyIf
import statusBarColour

private enum class ScaffoldSectionsEnum {
    HEADER, FOOTER, CONTENT, OVERLAY,
}

@Composable
fun FittoniaScaffold(
    content: @Composable ColumnScope.() -> Unit,
    header: (@Composable BoxScope.() -> Unit)? = null,
    footer: (@Composable BoxScope.() -> Unit)? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
    scrollable: Boolean = true,
) {
    val imeHeightLocal = imeHeight.collectAsState(initial = 0)
    val navBarHeightLocal = navBarHeight.collectAsState(initial = 0)
    val statusBarsHeightLocal = statusBarsHeight.collectAsState(initial = 0)
    Box(modifier = Modifier.background(backgroundLayer0Colour)) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(top = 100.dp, start = 100.dp)
                .clip(RoundedCornerShape(topStart = 500.dp))
                .fillMaxSize()
                .background(backgroundLayer1Colour),
        ) {}
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(top = 200.dp, start = 200.dp)
                .clip(RoundedCornerShape(topStart = 500.dp))
                .fillMaxSize()
                .background(backgroundLayer2Colour),
        ) {}
        SubcomposeLayout(modifier = Modifier) { constraints ->
            val headerPlaceables = subcompose(ScaffoldSectionsEnum.HEADER) {
                header?.let {
                    Column {
                        Box(
                            modifier = Modifier
                                .requiredHeight(statusBarsHeightLocal.value.toDp())
                                .fillMaxWidth()
                                .background(color = statusBarColour),
                        )
                        Box(
                            modifier = Modifier
                                .requiredHeight(50.dp)
                                .fillMaxWidth(),
                        ) {
                            header()
                        }
                    }
                } ?: Box {}
            }.single().measure(constraints)

            val footerPlaceables = subcompose(ScaffoldSectionsEnum.FOOTER) {
                footer?.let {
                    Column(modifier = Modifier.background(color = Color(0xAA448844))) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            footer()
                        }
                        Spacer(modifier = Modifier.requiredHeight(navBarHeightLocal.value.toDp()))
                    }
                } ?: Box {}
            }.single().measure(constraints)

            val contentPlaceables = subcompose(ScaffoldSectionsEnum.CONTENT) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.applyIf(scrollable) {
                        verticalScroll(scrollState)
                    },
                ) {
                    content()
                    Spacer(modifier = Modifier.requiredHeight(height = footerPlaceables.height.toDp()))
                }
            }.single().measure(
                constraints.copy(
                    maxHeight = run {
                        val systemBottomHeight = if (imeHeightLocal.value > 0) {
                            imeHeightLocal.value
                        } else {
                            navBarHeightLocal.value
                        }
                        constraints.maxHeight - headerPlaceables.height - systemBottomHeight
                    },
                ),
            )
            val overlayPlaceables = subcompose(ScaffoldSectionsEnum.OVERLAY) {
                Box(modifier = Modifier.fillMaxSize()) { overlay() }
            }.single().measure(constraints)

            layout(constraints.maxWidth, constraints.maxHeight) {
                headerPlaceables.place(x = 0, y = 0, zIndex = 1f)
                contentPlaceables.place(x = 0, y = headerPlaceables.height)
                footerPlaceables.let { footer ->
                    footer.place(
                        x = 0,
                        y = run {
                            val systemBottomHeight = if (imeHeightLocal.value > 0) {
                                imeHeightLocal.value
                            } else {
                                navBarHeightLocal.value
                            }
                            (constraints.maxHeight - footer.height + navBarHeightLocal.value) - systemBottomHeight
                        },
                        zIndex = 1f,
                    )
                }
                overlayPlaceables.place(x = 0, y = 0, zIndex = 2f)
            }
        }
    }
}
