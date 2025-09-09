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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.Navigator.Companion.NavigatorBackHandler
import org.hmeadow.fittonia.components.HeaderAndFooterDrawingConstants.BORDER_WIDTH
import org.hmeadow.fittonia.components.HeaderAndFooterDrawingConstants.CORNER_RADIUS
import org.hmeadow.fittonia.components.HeaderAndFooterDrawingConstants.shadowColours
import org.hmeadow.fittonia.compose.architecture.appStyleResetHeader
import org.hmeadow.fittonia.compose.architecture.appStyleResetStatusBar
import org.hmeadow.fittonia.compose.architecture.appStyleResetStatusFooter
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.mainActivity.LocalFocusRequester
import org.hmeadow.fittonia.mainActivity.MainActivity.Companion.imeHeight
import org.hmeadow.fittonia.mainActivity.MainActivity.Companion.imeHeightVar
import org.hmeadow.fittonia.mainActivity.MainActivity.Companion.mainActivity
import org.hmeadow.fittonia.mainActivity.MainActivity.Companion.navBarHeight
import org.hmeadow.fittonia.mainActivity.MainActivity.Companion.navBarHeightVar
import org.hmeadow.fittonia.mainActivity.MainActivity.Companion.statusBarsHeight
import org.hmeadow.fittonia.mainActivity.MainActivity.Companion.statusBarsHeightVar
import org.hmeadow.fittonia.utility.InfoBorderState.clearInfoBorderState
import org.hmeadow.fittonia.utility.applyIf
import org.hmeadow.fittonia.utility.dpToFloat

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
    NavigatorBackHandler()

    val imeHeightLocal = imeHeight.collectAsState(initial = imeHeightVar)
    val navBarHeightLocal = navBarHeight.collectAsState(initial = navBarHeightVar)
    val statusBarsHeightLocal = statusBarsHeight.collectAsState(initial = statusBarsHeightVar)
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
                    clearInfoBorderState()
                },
            ),
    ) {
        currentStyle.Background(modifier = Modifier)
        SubcomposeLayout(modifier = Modifier) { constraints ->
            val headerPlaceables = subcompose(ScaffoldSectionsEnum.HEADER) {
                header?.let {
                    Column {
                        key(appStyleResetStatusBar) {
                            Box(
                                modifier = Modifier
                                    .requiredHeight(height = statusBarsHeightLocal.value.toDp())
                                    .fillMaxWidth()
                                    .background(color = currentStyle.headerBackgroundColour)
                                    .drawWithCache {
                                        onDrawBehind {
                                            statusBarDraw()
                                        }
                                    },
                            )
                        }
                        key(appStyleResetHeader) {
                            Box(
                                modifier = Modifier
                                    .requiredHeight(50.dp)
                                    .fillMaxWidth(),
                            ) {
                                header()
                            }
                        }
                    }
                } ?: Box {}
            }.single().measure(constraints = constraints)

            val footerPlaceables = subcompose(ScaffoldSectionsEnum.FOOTER) {
                footer?.let {
                    key(appStyleResetStatusFooter) {
                        Column(
                            modifier = Modifier
                                .background(
                                    color = currentStyle.footerBackgroundColour,
                                    shape = RoundedCornerShape(
                                        topStart = CORNER_RADIUS.dp,
                                        topEnd = CORNER_RADIUS.dp,
                                        bottomEnd = 0.dp,
                                        bottomStart = 0.dp,
                                    ),
                                )
                                .drawWithCache {
                                    onDrawBehind {
                                        footerDraw()
                                    }
                                }
                                .padding(horizontal = 15.dp),
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                footer()
                            }
                            Spacer(modifier = Modifier.requiredHeight(navBarHeightLocal.value.toDp()))
                        }
                    }
                } ?: Box(
                    modifier = Modifier
                        .requiredHeight(height = navBarHeightLocal.value.toDp())
                        .fillMaxWidth()
                        .background(
                            color = currentStyle.footerBackgroundColour,
                            shape = RoundedCornerShape(
                                topStart = CORNER_RADIUS.dp,
                                topEnd = CORNER_RADIUS.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp,
                            ),
                        )
                        .drawWithCache {
                            onDrawBehind {
                                footerDraw()
                            }
                        },
                ) {}
            }.single().measure(constraints = constraints)

            val contentPlaceables = subcompose(ScaffoldSectionsEnum.CONTENT) {
                val scrollState = rememberScrollState()
                Column(
                    // TODO add horizontal padding in landscape mode to avoid in-screen camera aperture.
                    modifier = Modifier
                        .applyIf(condition = scrollable) {
                            verticalScroll(scrollState)
                        },
                ) {
                    content(footerPlaceables.height.toDp())
                    Spacer(modifier = Modifier.requiredHeight(height = footerPlaceables.height.toDp()))
                }
            }.single().measure(
                constraints = constraints.copy(
                    maxHeight = constraints.maxHeight
                        .minus(headerPlaceables.height)
                        .minus(systemBottomHeight)
                        .plus(navBarHeightLocal.value),
                ),
            )
            val overlayPlaceables = subcompose(ScaffoldSectionsEnum.OVERLAY) {
                Box(modifier = Modifier.fillMaxSize()) { overlay() }
            }.single().measure(constraints = constraints)

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

private fun DrawScope.statusBarDraw() {
    val shadowWidth = mainActivity.dpToFloat(CORNER_RADIUS.dp)
    val width = this.size.width
    val height = this.size.height

    // Left side.
    run {
        // Shadow.
        drawRect(
            brush = Brush.linearGradient(
                colors = shadowColours,
                start = Offset(x = shadowWidth, y = 0f),
                end = Offset.Zero,
            ),
            topLeft = Offset.Zero,
            size = Size(width = shadowWidth, height = height),
        )
        // Border.
        drawLine(
            color = currentStyle.headerAndFooterBorderColour,
            start = Offset.Zero,
            end = Offset(x = 0f, y = height),
            strokeWidth = BORDER_WIDTH,
        )
    }

    // Right side.
    run {
        // Shadow.
        drawRect(
            brush = Brush.linearGradient(
                colors = shadowColours,
                start = Offset(x = width - shadowWidth, y = 0f),
                end = Offset(x = width, y = 0f),
            ),
            topLeft = Offset(x = width - shadowWidth, y = 0f),
            size = Size(width = shadowWidth, height = height),
        )
        // Border.
        drawLine(
            color = currentStyle.headerAndFooterBorderColour,
            start = Offset(x = width, y = 0f),
            end = Offset(x = width, y = height),
            strokeWidth = BORDER_WIDTH,
        )
    }
}

private fun DrawScope.footerDraw() {
    val cornerRadius = mainActivity.dpToFloat(CORNER_RADIUS.dp)
    val cornerDiameter = cornerRadius * 2
    val width = this.size.width
    val height = this.size.height
    val cornerSize = Size(width = cornerDiameter, height = cornerDiameter)

    // Left side.
    run {
        // Shadow.
        drawRect(
            brush = Brush.linearGradient(
                colors = shadowColours,
                start = Offset(x = cornerRadius, y = 0f),
                end = Offset.Zero,
            ),
            topLeft = Offset(x = 0f, y = cornerRadius),
            size = Size(width = width, height = height),
        )
        // Border.
        drawLine(
            color = currentStyle.headerAndFooterBorderColour,
            start = Offset(x = 0f, y = cornerRadius),
            end = Offset(x = 0f, y = height),
            strokeWidth = BORDER_WIDTH,
        )
    }

    // Right side.
    run {
        // Shadow.
        drawRect(
            brush = Brush.linearGradient(
                colors = shadowColours,
                start = Offset(x = width - cornerRadius, y = 0f),
                end = Offset(x = width, y = 0f),
            ),
            topLeft = Offset(x = width - cornerRadius, y = cornerRadius),
            size = Size(width = cornerRadius, height = height),
        )
        // Border.
        drawLine(
            color = currentStyle.headerAndFooterBorderColour,
            start = Offset(x = width, y = cornerRadius),
            end = Offset(x = width, y = height),
            strokeWidth = BORDER_WIDTH,
        )
    }

    // Top side.
    run {
        // Shadow.
        drawRect(
            brush = Brush.linearGradient(
                colors = shadowColours,
                start = Offset(x = 0f, y = cornerRadius),
                end = Offset.Zero,
            ),
            topLeft = Offset(x = cornerRadius, y = 0f),
            size = Size(width = width - cornerDiameter, height = cornerRadius),
        )

        // Border.
        drawLine(
            color = currentStyle.headerAndFooterBorderColour,
            start = Offset(x = cornerRadius, y = 0f),
            end = Offset(x = width - cornerRadius, y = 0f),
            strokeWidth = BORDER_WIDTH,
        )
    }

    // Left corner.
    run {
        // Shadow.
        drawArc(
            brush = Brush.radialGradient(
                colors = shadowColours,
                center = Offset(x = cornerRadius, y = cornerRadius),
                radius = cornerRadius,
            ),
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true,
            size = cornerSize,
            topLeft = Offset.Zero,
        )
        // Border.
        drawArc(
            color = currentStyle.headerAndFooterBorderColour,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            size = cornerSize,
            topLeft = Offset.Zero,
            style = Stroke(width = BORDER_WIDTH),
        )
    }

    // Right corner.
    run {
        // Shadow.
        drawArc(
            brush = Brush.radialGradient(
                colors = shadowColours,
                center = Offset(x = width - cornerRadius, y = cornerRadius),
                radius = cornerRadius,
            ),
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true,
            size = cornerSize,
            topLeft = Offset(x = width - cornerDiameter, y = 0f),
        )

        // Border.
        drawArc(
            color = currentStyle.headerAndFooterBorderColour,
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            size = cornerSize,
            topLeft = Offset(x = width - cornerDiameter, y = 0f),
            style = Stroke(width = BORDER_WIDTH),
        )
    }
}
