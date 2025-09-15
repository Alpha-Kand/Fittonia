package org.hmeadow.fittonia.screens.overviewScreen

import LogType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import org.hmeadow.fittonia.AppLogs
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.fonts.headingMStyle

@Composable
internal fun OverviewScreenLogsTab(
    maxWidth: Dp,
    maxHeight: Dp,
) {
    val lazyListState = rememberLazyListState()
    Column(
        modifier = Modifier
            .width(width = maxWidth)
            .height(height = maxHeight)
            .padding(horizontal = spacing16),
    ) {
        Text(text = "Logs", style = headingMStyle)

        LazyColumn(
            state = lazyListState,
        ) {
            itemsIndexed(AppLogs.logs) { index, log ->
                Text(
                    modifier = Modifier
                        .background(color = if (index % 2 == 0) Color.White else Color(0xFFEEEEEE))
                        .padding(vertical = spacing4)
                        .fillMaxWidth(),
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = when (log.type) {
                                    LogType.NORMAL -> Color(0xFF666666)
                                    LogType.WARNING -> Color.Yellow
                                    LogType.ERROR -> Color.Red
                                    LogType.DEBUG -> Color.Blue
                                },
                            ),
                        ) {
                            append(
                                log.timeStampShort + " " + when (log.type) {
                                    LogType.NORMAL -> ""
                                    LogType.WARNING -> "WARNING "
                                    LogType.ERROR -> "ERROR "
                                    LogType.DEBUG -> "DEBUG "
                                },
                            )
                        }
                        append(log.message)
                    },
                )
            }
        }

        LaunchedEffect(AppLogs.logs.size) {
            lazyListState.animateScrollToItem(AppLogs.logs.lastIndex)
        }
    }
}
