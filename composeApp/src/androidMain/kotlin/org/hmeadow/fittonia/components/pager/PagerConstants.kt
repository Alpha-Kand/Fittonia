package org.hmeadow.fittonia.components.pager

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.utility.inchesToPx
import kotlin.math.absoluteValue

object PagerConstants {
    /** How much to 'shrink' the indicator's width in from the full width of the text it's currently under. */
    val indicatorHorizontalPadding = 16.dp

    /** Padding around the entire rendered text tabs. */
    val renderedTextPadding = 16.dp

    /** Required swipe velocity to actually scroll to next tab. Helps prevent scrolling by light touches. */
    const val MIN_VELOCITY_REQUIRED_TO_SCROLL = 1000f

    /** Required swipe distance to actually scroll to next tab. Helps prevent scrolling by light bumbs. */
    val minSwipeDistanceToTriggerScrollPx: Int
        @Composable
        get() = MainActivity.mainActivity.inchesToPx(inches = 0.3f).toInt()

    /**
     * If the user scrolls the tabs so the indicator disappears off the side of the screen, it scrolls back into view
     * after a short delay.
     */
    const val SCROLL_INDICATOR_INTO_VIEW_DELAY = 2000L
}

/** Returns the [LazyListItemInfo] with the smallest offset. */
val LazyListState.minAbsOffset: LazyListItemInfo?
    get() = this.layoutInfo.visibleItemsInfo.minByOrNull { it.offset.absoluteValue }
