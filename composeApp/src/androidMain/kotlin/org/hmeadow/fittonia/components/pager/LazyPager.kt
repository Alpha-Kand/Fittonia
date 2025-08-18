package org.hmeadow.fittonia.components.pager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.scrollToIndex
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext
import org.hmeadow.fittonia.components.pager.PagerConstants.MIN_VELOCITY_REQUIRED_TO_SCROLL
import org.hmeadow.fittonia.components.pager.PagerConstants.minSwipeDistanceToTriggerScrollPx
import org.hmeadow.fittonia.components.pager.PagerState.Companion.rememberPagerState
import org.hmeadow.fittonia.utility.applyIf
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * A Pager implementation based on the Lazy scrollable components.
 *
 * @param pageShrinkAmount Horizontal padding to be applied to each page, shrinking it inwards.
 * @param spacing Space between each page.
 * @param fillMaxHeight Whether or not to attempt to fill max height.
 * @param requiredWidth If set, will attempt to set pager to this width.
 * @param reverseLayout If true, pages will scroll in opposite direction, (still in the same order as non-reversed).
 * @param onSwipe Callback when scrolling to another page. New page index is passed to the callback.
 */
@Composable
fun LazyPager(
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(),
    spacing: Dp = 0.dp,
    pageShrinkAmount: Dp = 0.dp,
    fillMaxHeight: Boolean = false,
    requiredWidth: Dp? = null,
    reverseLayout: Boolean = false,
    onSwipe: (Int) -> Unit = {},
    content: LazyListScope.() -> Unit,
) = LocalDensity.current.run {
    val listState = pagerState.listState
    val targetIndex = pagerState.targetPage

    /** How much the user scrolls per drag/swipe. Resets on finger up. */
    var swipeOffset by remember { mutableFloatStateOf(0f) }

    val minimumScrollAmount = minSwipeDistanceToTriggerScrollPx
    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer { this.clip = true }
            .nestedScroll(
                connection = object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        pagerState.captureCurrentVisiblePage()
                        swipeOffset -= available.x
                        return super.onPreScroll(available, source)
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (available.y.absoluteValue > available.x.absoluteValue) {
                            return super.onPreFling(available)
                        }
                        val isScrollingHardEnough = available.x.absoluteValue > MIN_VELOCITY_REQUIRED_TO_SCROLL
                        val hasScrolledFarEnough = abs(swipeOffset) > minimumScrollAmount
                        pagerState.performSwipeScroll(
                            requirement = isScrollingHardEnough || hasScrolledFarEnough,
                            onSuccess = onSwipe,
                        )
                        swipeOffset = 0f
                        return available
                    }
                },
            )
            .semantics {
                scrollToIndex {
                    pagerState.targetPage = it
                    true
                }
            },
    ) {
        val mLeakSize = pageShrinkAmount.coerceAtLeast(0.dp)
        LazyRow(
            reverseLayout = reverseLayout,
            contentPadding = remember(mLeakSize) { PaddingValues(horizontal = mLeakSize) },
            state = listState,
            content = {
                content(
                    LazyPagerScope(
                        modifier = Modifier
                            .width(requiredWidth ?: (maxWidth - (mLeakSize * 2)).coerceAtLeast(1.dp))
                            .applyIf(fillMaxHeight) {
                                fillMaxHeight()
                            },
                        scope = this,
                    ),
                )
            },
            horizontalArrangement = Arrangement.spacedBy(spacing),
        )
    }
    LaunchedEffect(listState, targetIndex) {
        snapshotFlow { listState }
            .filter {
                !it.isScrollInProgress && targetIndex != it.firstVisibleItemIndex && targetIndex >= 0
            }
            .collect {
                withContext(NonCancellable) {
                    pagerState.scrollToPage(index = targetIndex)
                }
            }
    }
}
