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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R

@Composable
fun ReadOnlyEntries(
    entries: List<String>,
    onEntryClearClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLines: Boolean = true,
    expandOnClick: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .border(width = 2.dp, color = Color(0xFF446644))
            .background(color = Color(0xFFDDFFEE))
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
                )

                if (text.isNotEmpty()) {
                    FittoniaIcon(
                        modifier = Modifier
                            .requiredSize(14.dp)
                            .clickable { onEntryClearClicked(text) },
                        drawableRes = R.drawable.ic_clear,
                        tint = Color(0xFF222222),
                    )
                }
            }
            if (index != entries.lastIndex) {
                HorizontalLine()
            }
        }
    }
}
