package org.hmeadow.fittonia.components.pager

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.math.sign

/**
 * A state object that is used to control a Pager.
 *
 * @param initialPage The default page to show when the Pager component is first loaded.
 * @param shouldAnimate Whether or not the scrolling should be animated.
 * @property listState A [LazyListState] that controls the Pager's scrolling capabilities.
 * @property targetPage Represents a transition state to a specific page the [PagerState] will attempt to scroll to. Do
 * not assume reading this value represents the actual scrolling position, use [pagePosition] instead for that.
 * @property pagePosition Represents the practical scrolling position and focues page. The int value
 * (e.g. `1` in `1.23`) acts as an index for the currently focused page. The decimal value (e.g. `0.23` in `1.23`)
 * represents scrolling 'in-between' two pages. e.g. `1.5` means the [PagerState] is scrolling halfway between indices 1
 * and 2.
 */
class PagerState(initialPage: Int = 0, val shouldAnimate: Boolean) {
    val listState: LazyListState = LazyListState(firstVisibleItemIndex = initialPage)
    var targetPage: Int
        get() = transitionState.targetState
        set(value) {
            transitionState.targetState = value
        }
    val pagePosition: Float
        get() = focusedItemOffset?.let { centerItem ->
            (centerItem.index + (1f - (centerItem.value.toFloat() / pageWidth))) - 1f
        } ?: listState.firstVisibleItemIndex.toFloat()

    /**
     * The current visible page as the user starts a new scroll. Needed to figure out the next page the user is trying
     * to scroll to.
     */
    private var capturedVisiblePage: LazyListItemInfo? = null
    private val transitionState = MutableTransitionState(initialPage)
    private val focusedItemOffset: IndexedValue<Int>?
        get() = derivedStateOf {
            listState.minAbsOffset?.let {
                IndexedValue(it.index, it.offset)
            }
        }.value
    private val pageWidth: Int
        get() = listState.layoutInfo.let { it.viewportEndOffset - it.viewportStartOffset }

    /**
     * Alternative to setting [targetPage] directly.
     * @param page Page the Pager should scroll to.
     */
    fun goToPage(page: Int) {
        transitionState.targetState = page
    }

    /**
     * Scrolls the pager list to the given index.
     */
    suspend fun scrollToPage(index: Int) {
        if (shouldAnimate) {
            listState.animateScrollToItem(
                index = index,
                scrollOffset = 0,
            )
        } else {
            listState.scrollToItem(
                index = index,
                scrollOffset = 0,
            )
        }
    }

    /**
     * Prepares the pager to be able to determine which new page the user is trying to scroll to.
     * Required to be called during `NestedScrollConnection.onPreScroll`.
     */
    fun captureCurrentVisiblePage() {
        if (capturedVisiblePage == null) {
            capturedVisiblePage = listState.minAbsOffset
        }
    }

    /**
     * Scrolls the pager to the next page the user is trying to scroll to if [requirement] is true. Otherwise, resets
     * pager position to focus on current page. Required to be called in `NestedScrollConnection.onPreFling`.
     * @param requirement Only scrolls to next page if `true`.
     * @param onSuccess Called only when the pager scrolls to a new page. Passes the new index to the callback.
     */
    suspend fun performSwipeScroll(requirement: Boolean, onSuccess: (Int) -> Unit) {
        if(requirement) {
            val index = listState.findByIndex(capturedVisiblePage?.index)?.let {
                it.index - it.offset.sign
            } ?: listState.minAbsOffset?.index ?: 0
            onSuccess(index)
            scrollToPage(index = index)
        }else {
            capturedVisiblePage?.index?.let {
                scrollToPage(index = it)
            }
        }
        capturedVisiblePage = null
    }

    companion object {
        /**
         * Creates a [PagerState] that is remembered across recompositions.
         * @param initialPage The default page to show when a Pager component that takes this [PagerState] is first
         * loaded.
         * @param shouldAnimate Whether or not the scrolling should be animated.
         */
        @Composable
        fun rememberPagerState(initialPage: Int = 0, shouldAnimate: Boolean = true): PagerState {
            val pagerState = rememberSaveable(saver = generateSaver()) {
                PagerState(initialPage = initialPage, shouldAnimate = shouldAnimate)
            }

            // Automatically update 'targetPage' as the user scrolls the pager.
            val offset = pagerState.listState.minAbsOffset
            LaunchedEffect(offset?.offset) {
                if (offset?.offset != null && pagerState.listState.findByIndex(offset.index) != null) {
                    pagerState.targetPage = offset.index
                }
            }

            return pagerState
        }

        private fun generateSaver() = Saver<PagerState, Pair<Int, Boolean>>(
            save = { it.transitionState.currentState to it.shouldAnimate },
            restore = { PagerState(initialPage = it.first, shouldAnimate = it.second) },
        )

        /**
         * Searches LazyListState's visible items for an item with the given index.
         */
        private fun LazyListState.findByIndex(index: Int?): LazyListItemInfo? {
            if (index == null) return null
            return this.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        }
    }
}
