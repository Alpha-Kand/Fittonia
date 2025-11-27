package org.hmeadow.fittonia.components.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hmeadow.fittonia.components.pager.PagerConstants.SCROLL_INDICATOR_INTO_VIEW_DELAY
import org.hmeadow.fittonia.components.pager.PagerConstants.indicatorHorizontalPadding
import org.hmeadow.fittonia.components.pager.PagerConstants.renderedTextPadding
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.design.Spacing.spacing2
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * This component renders tab labels for a pager.
 *
 * @param position Specifies where the indicator should be drawn. The int value (e.g. `1` in `1.23`) acts as an index
 * for the currently focused tab. The decimal value (e.g. `0.23` in `1.23`) informs where the indicator should be
 * 'in-between' two tabs. e.g. `1.5` means the indicator is halfway between indices 1 and 2.
 * @param tabs List of tab names in the order they should be drawn in.
 * @param onTabSelected Callback when a tab is clicked.
 * @param tabLabelContent A composable that will be used for each label. Is passed the tab's text and whether or not
 * it's currently selected.
 * @param indicatorContent A composable representing the indicator. `leftOffset` is how much padding to add to the left
 * of the indicator to position it properly. `Width` is the width of the currently selected tab label.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerTabLabels(
    position: Float,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    tabLabelContent: @Composable (String, Boolean) -> Unit = { text, isSelected ->
        DefaultTabLabel(text = text, isSelected = isSelected)
    },
    indicatorContent: @Composable (Dp, Dp) -> Unit = { leftOffset, width ->
        DefaultIndicator(leftOffset = leftOffset, width = width)
    },
) {
    /* Width of the visible portion of the component in pixels. */
    var visibleWidth by remember { mutableIntStateOf(0) }
    /* Coroutine that helps scroll the tab indicator back into view if it was scrolled out of view. */
    var scrollIndicatorIntoViewJob by remember { mutableStateOf<Job?>(null) }
    /* Right edge of the indicator from the start of the component in pixels. */
    var indicatorRightEdge by remember { mutableIntStateOf(-1) }
    /* Left edge of the indicator from the start of the component in pixels. */
    var indicatorLeftEdge by remember { mutableIntStateOf(-1) }

    Box(
        modifier = Modifier.nestedScroll(
            connection = object : NestedScrollConnection {
                override suspend fun onPreFling(available: Velocity): Velocity {
                    when {
                        // Indicator scrolled off to the left.
                        scrollState.value > indicatorLeftEdge -> indicatorLeftEdge

                        // Indicator scrolled off to the right.
                        visibleWidth + scrollState.value < indicatorRightEdge -> indicatorRightEdge - visibleWidth

                        else -> {
                            scrollIndicatorIntoViewJob?.cancelAndJoin()
                            null
                        }
                    }?.let { value ->
                        withContext(Dispatchers.IO) {
                            scrollIndicatorIntoViewJob?.cancelAndJoin()
                            scrollIndicatorIntoViewJob = launch {
                                delay(timeMillis = SCROLL_INDICATOR_INTO_VIEW_DELAY)
                                scrollState.animateScrollTo(value)
                            }
                        }
                    }
                    return super.onPreFling(available)
                }
            },
        ),
    ) {
        Box(
            modifier = modifier
                .horizontalScroll(state = scrollState)
                .fillMaxWidth()
                .onGloballyPositioned { visibleWidth = it.size.width - scrollState.maxValue },
        ) {
            val requesters = remember(tabs) {
                tabs.map { BringIntoViewRequester() }
            }
            SubcomposeLayout { constraints ->
                var tabNamesMaxHeight = constraints.minHeight
                var tabNamesCombinedWidth = 0
                // Render tab names.
                val tabLayouts = tabs.mapIndexed { index, text ->
                    subcompose(slotId = text) {
                        TabLabel(
                            isSelected = !position.isNaN() && index == position.roundToInt(),
                            text = text,
                            index = index,
                            bringIntoViewRequester = requesters[index],
                            onTabLabelClicked = {
                                scrollIndicatorIntoViewJob?.cancel()
                                onTabSelected(it)
                            },
                            tabContent = tabLabelContent,
                        )
                    }.single().measure(constraints).also {
                        tabNamesMaxHeight = max(tabNamesMaxHeight, it.height)
                        if (index == position.roundToInt()) {
                            indicatorLeftEdge = tabNamesCombinedWidth
                        }
                        tabNamesCombinedWidth += it.width
                        if (index == position.roundToInt()) {
                            indicatorRightEdge = tabNamesCombinedWidth
                        }
                    }
                }
                // Render indicator.
                val indicator = subcompose(slotId = "INDICATOR_KEY") {
                    val leftTab = tabLayouts.getRect(
                        index = floor(position).toInt(),
                        horizontalPadding = indicatorHorizontalPadding.toPx(),
                    )
                    val rightTab = tabLayouts.getRect(
                        index = ceil(position).toInt(),
                        horizontalPadding = indicatorHorizontalPadding.toPx(),
                    )
                    val indicatorPosition = lerp(start = leftTab, stop = rightTab, fraction = position % 1)
                    indicatorContent(indicatorPosition.left.toDp(), indicatorPosition.width.toDp())
                }.single().measure(constraints)

                val tabSeparators = (0..<tabs.size).map { index ->
                    subcompose(slotId = "SEPARATOR$index") {
                        Box(
                            modifier = Modifier
                                .background(color = currentStyle.headerAndFooterBorderColour)
                                .width(width = spacing2)
                                .height(height = getTabLabelHeight())
                                .align(alignment = Alignment.CenterEnd),
                        ) {}
                    }.single().measure(constraints)
                }

                layout(width = tabNamesCombinedWidth, height = tabNamesMaxHeight) {
                    var offset = 0
                    tabLayouts.fastForEachIndexed { index, placeable ->
                        placeable.placeRelative(x = offset, y = 0)
                        offset += placeable.width
                        if (index != tabLayouts.lastIndex) {
                            tabSeparators[index].placeRelative(x = offset, y = 16.dp.toPx().toInt()) // TODO constant
                        }
                    }
                    indicator.placeRelative(x = 0, y = tabNamesMaxHeight - (indicator.height * 2))
                }
            }
        }
    }
}

/**
 *  Returns height of tab text labels to help draw the tab separators.
 */
@Composable
private fun getTabLabelHeight(): Dp {
    val density = LocalDensity.current
    val textStyle = paragraphTextStyle
    val textMeasurer = rememberTextMeasurer()
    return remember {
        with(density) {
            textMeasurer.measure(text = "I", style = textStyle).size.height.toDp()
        }
    }
}

/**
 * Gets the [Rect] bounds of the tab name at the given index.
 * @param index Index in the list to get the [Rect] of.
 * @param horizontalPadding Desired horizontal padding in pixels.
 */
private fun List<Placeable>.getRect(index: Int, horizontalPadding: Float): Rect {
    val offsetLeft = asSequence().take(index).sumOf { it.width }.toFloat()
    return getOrNull(index)?.let {
        Rect(
            left = offsetLeft + horizontalPadding,
            top = 0f,
            right = offsetLeft + it.width - horizontalPadding,
            bottom = it.height.toFloat(),
        )
    } ?: Rect.Zero
}

/**
 * Composable to be drawn for each tab.
 * @param isSelected Whether or not the tab is currently selected.
 * @param index Index of the tab.
 * @param text Text associated with the index of the tab.
 * @param onTabLabelClicked Callback for when the label is clicked.
 * @param tabContent Composable to render for each tab label.
 */
@Composable
@ExperimentalFoundationApi
private fun TabLabel(
    isSelected: Boolean,
    index: Int,
    text: String,
    bringIntoViewRequester: BringIntoViewRequester,
    onTabLabelClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tabContent: @Composable (String, Boolean) -> Unit,
) {
    LaunchedEffect(isSelected) {
        if (isSelected) {
            bringIntoViewRequester.bringIntoView()
        }
    }
    Box(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .clickable(!isSelected) {
                onTabLabelClicked(index)
            }
            .padding(all = renderedTextPadding),
    ) {
        tabContent(text, isSelected)
    }
}

/**
 * Draws a tab position indicator. Note that the indicator's 'position' is simulated by adding padding to the front,
 * and not by moving the entire indicator component.
 *
 * @param leftOffset How much padding to add to the left.
 * @param width The width of the currently selected tab label.
 */
@Composable
fun DefaultIndicator(leftOffset: Dp, width: Dp) {
    Box(
        modifier = Modifier
            .padding(start = leftOffset)
            .width(width = width)
            .height(height = 5.dp)
            .background(
                color = Color.Black,
                shape = RoundedCornerShape(
                    topStartPercent = 40,
                    topEndPercent = 40,
                    bottomStartPercent = 40,
                    bottomEndPercent = 40,
                ),
            ),
    )
}

/**
 * Default component for each tab label.
 *
 * @param text Text for the label.
 * @param isSelected Whether or not the label is focuses/selected.
 */
@Composable
fun DefaultTabLabel(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = if (isSelected) {
            Color.Black
        } else {
            Color.DarkGray
        },
        modifier = modifier,
    )
}
