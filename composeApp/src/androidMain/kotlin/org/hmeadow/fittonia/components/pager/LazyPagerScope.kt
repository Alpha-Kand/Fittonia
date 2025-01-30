package org.hmeadow.fittonia.components.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Scope for a [LazyPager] that contains all of the pages. Wraps each page with a special modifier that dictates it's
 * pager-specific dimensions and appearance.
 *
 * @param modifier Modifier to pass to each page individually.
 * @param scope [LazyListScope] the pages actually live in.
 */
class LazyPagerScope(
    private val modifier: Modifier,
    private val scope: LazyListScope,
) : LazyListScope {

    override fun item(
        key: Any?,
        contentType: Any?,
        content: @Composable LazyItemScope.() -> Unit,
    ) {
        scope.item(key) {
            Box(this@LazyPagerScope.modifier) {
                content()
            }
        }
    }

    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: @Composable LazyItemScope.(index: Int) -> Unit,
    ) {
        scope.items(count, key) {
            Box(this@LazyPagerScope.modifier) {
                itemContent(it)
            }
        }
    }

    @ExperimentalFoundationApi
    override fun stickyHeader(key: Any?, contentType: Any?, content: @Composable LazyItemScope.() -> Unit) {
        scope.stickyHeader(key) {
            Box(this@LazyPagerScope.modifier) {
                content()
            }
        }
    }
}
