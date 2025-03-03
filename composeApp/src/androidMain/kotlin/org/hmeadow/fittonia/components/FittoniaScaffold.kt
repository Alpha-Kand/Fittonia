package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.LocalFocusRequester
import org.hmeadow.fittonia.MainActivity.Companion.imeHeight
import org.hmeadow.fittonia.MainActivity.Companion.navBarHeight
import org.hmeadow.fittonia.MainActivity.Companion.statusBarsHeight
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.utility.applyIf

private enum class ScaffoldSectionsEnum {
    HEADER, FOOTER, CONTENT, OVERLAY,
}

@Composable
fun FittoniaScaffold(
    content: @Composable ColumnScope.(footerHeight: Dp) -> Unit,
    header: (@Composable BoxScope.() -> Unit)? = null,
    footer: (@Composable BoxScope.() -> Unit)? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
    scrollable: Boolean = true,
) {
    val imeHeightLocal = imeHeight.collectAsState(initial = 0)
    val navBarHeightLocal = navBarHeight.collectAsState(initial = 0)
    val statusBarsHeightLocal = statusBarsHeight.collectAsState(initial = 0)
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = LocalFocusRequester.current
    val systemBottomHeight = if (imeHeightLocal.value > 0) {
        imeHeightLocal.value
    } else {
        navBarHeightLocal.value
    }
    Box(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    keyboard?.hide()
                    focusRequester.requestFocus()
                },
            ),
    ) {
        currentStyle.Background()
        SubcomposeLayout(modifier = Modifier) { constraints ->
            val headerPlaceables = subcompose(ScaffoldSectionsEnum.HEADER) {
                header?.let {
                    Column {
                        Box(
                            modifier = Modifier
                                .requiredHeight(statusBarsHeightLocal.value.toDp())
                                .fillMaxWidth()
                                .background(color = currentStyle.statusBarColour),
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
                    Column(modifier = Modifier.background(color = currentStyle.footerBackgroundColour)) {
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
                    content(footerPlaceables.height.toDp())
                    Spacer(modifier = Modifier.requiredHeight(height = footerPlaceables.height.toDp()))
                }
            }.single().measure(
                constraints.copy(
                    maxHeight = constraints.maxHeight
                        .minus(headerPlaceables.height)
                        .minus(systemBottomHeight)
                        .plus(navBarHeightLocal.value),
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
                        y = constraints.maxHeight
                            .minus(footer.height)
                            .minus(systemBottomHeight)
                            .plus(navBarHeightLocal.value),
                        zIndex = 1f,
                    )
                }
                overlayPlaceables.place(x = 0, y = 0, zIndex = 2f)
            }
        }
    }
}
