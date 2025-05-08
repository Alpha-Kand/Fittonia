package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.compose.architecture.currentStyle

@Composable
fun ReadOnlyEntries(
    entries: List<String>,
    modifier: Modifier = Modifier,
    onEntryClearClicked: ((String) -> Unit)? = null,
    singleLines: Boolean = true,
    expandOnClick: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .border(
                width = 2.dp,
                color = currentStyle.readOnlyBorderColour,
                shape = RoundedCornerShape(corner = CornerSize(5.dp)),
            )
            .background(
                color = currentStyle.readOnlyBackgroundColour,
                shape = RoundedCornerShape(corner = CornerSize(5.dp)),
            )
            .clickable {
                if (expandOnClick) {
                    expanded = !expanded
                }
            },
    ) {
        entries.forEachIndexed { index, text ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 5.dp),
                verticalAlignment = CenterVertically,
            ) {
                Text(
                    modifier = if (singleLines && !expanded) {
                        Modifier
                            .weight(1.0f)
                            .horizontalScroll(rememberScrollState())
                    } else {
                        Modifier.weight(1.0f)
                    },
                    text = text,
                    overflow = TextOverflow.Clip,
                    maxLines = if (singleLines && !expanded) 1 else Int.MAX_VALUE,
                    style = textStyle,
                )

                if (text.isNotEmpty()) {
                    onEntryClearClicked?.let {
                        FittoniaIcon(
                            modifier = Modifier
                                .requiredSize(14.dp)
                                .clickable { onEntryClearClicked(text) },
                            drawableRes = R.drawable.ic_clear,
                            tint = currentStyle.readOnlyClearIconColour,
                        )
                    }
                }
            }
            if (index != entries.lastIndex) {
                HorizontalLine()
            }
        }
    }
}

@Composable
fun ReadOnlyEntries(
    entries: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(width = 2.dp, color = currentStyle.readOnlyBorderColour)
            .background(color = currentStyle.readOnlyBackgroundColour),
    ) {
        entries.forEachIndexed { index, content ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 5.dp),
                verticalAlignment = CenterVertically,
            ) {
                content()
            }
            if (index != entries.lastIndex) {
                HorizontalLine()
            }
        }
    }
}
