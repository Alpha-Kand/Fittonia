package org.hmeadow.fittonia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaBackground
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightColumn
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.headingLStyle
import org.hmeadow.fittonia.components.headingMStyle
import kotlin.random.Random

val transfer = TransferJob(
    description = "Foo",
    destination = "192.56.43.01",
    items = 36,
    progress = 0.5,
    status = TransferStatus.Sending,
    direction = TransferJob.Direction.OUTGOING,
)

@Composable
@Preview
fun TransferDetailsScreen() {
    FittoniaBackground(
        content = {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Text(
                    text = transfer.description,
                    style = headingLStyle,
                )

                HMSpacerHeight(height = 40)

                Text(
                    text = "Destination",
                    style = headingMStyle,
                )

                HMSpacerHeight(height = 5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp),
                ) {
                    Text(
                        text = transfer.destination,
                    )
                }
                HMSpacerHeight(height = 30)

                Text(
                    text = "Status",
                    style = headingMStyle,
                )

                HMSpacerHeight(height = 5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp),
                ) {
                    Row {
                        FittoniaIcon(
                            drawableRes = when (transfer.status) {
                                TransferStatus.Sending -> R.drawable.ic_arrow_send
                                TransferStatus.Receiving -> R.drawable.ic_arrow_receive
                                TransferStatus.Error -> R.drawable.ic_alert
                                TransferStatus.Done -> R.drawable.ic_checkmark
                            },
                            tint = when (transfer.status) {
                                TransferStatus.Sending -> Color(0xFF0000FF)
                                TransferStatus.Receiving -> Color(0xFF0000FF)
                                TransferStatus.Error -> Color(0xFFFFFF00)
                                TransferStatus.Done -> Color(0xFF00FF00)
                            },
                        )

                        Text(
                            text = when (transfer.status) {
                                TransferStatus.Sending -> "Sending"
                                TransferStatus.Receiving -> "Receiving"
                                TransferStatus.Error -> "Error"
                                TransferStatus.Done -> "Done!"
                            },
                        )
                    }
                }
                HMSpacerHeight(height = 30)

                Text(
                    text = "Progress",
                    style = headingMStyle,
                )

                HMSpacerHeight(height = 5)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp),
                ) {
                    val aaa = when (transfer.status) {
                        TransferStatus.Sending -> "sent"
                        TransferStatus.Receiving -> "received"
                        else -> ""
                    }
                    Row {
                        Text(
                            text = "${rememberPercentageFormat(transfer.progress)} - 4 out of ${transfer.items} items $aaa.",
                        )
                    }
                }

                HMSpacerHeight(height = 30)

                Text(
                    text = "Logs",
                    style = headingMStyle,
                )

                HMSpacerHeight(height = 5)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp),
                ) {
                    val fileList = listOf(
                        "File.txt" to "aaa/bbb/ccc/File.txt",
                        "video.mp4" to "funniest home videos/video.mp4",
                        "song.mp3" to "guns and roses/music/song.mp3",
                    )
                    fileList.forEachIndexed { index, file ->
                        var fooState by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .clickable {
                                    fooState = !fooState
                                }
                                .fillMaxWidth(),
                        ) {
                            val foo = when (transfer.status) {
                                TransferStatus.Sending -> ""
                                TransferStatus.Receiving -> ""
                                TransferStatus.Error -> "Error "
                                TransferStatus.Done -> "Done "
                            }
                            Row {
                                if (fooState) {
                                    Text(
                                        text = file.second,
                                    )
                                    HMSpacerWeightRow()
                                } else {
                                    Text(
                                        text = "${file.first} : $foo",
                                    )
                                    HMSpacerWeightRow()
                                    Text(
                                        text = rememberPercentageFormat(Random.nextDouble(1.0)),
                                    )
                                }
                                FittoniaIcon(
                                    drawableRes = if (fooState) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down,
                                    tint = Color(0xFF222222),
                                )
                            }
                            if (fooState) {
                                val x = Random.nextInt(4096)
                                val y = Random.nextInt(100) * x
                                Row {
                                    Text(
                                        text = "$foo${rememberPercentageFormat(Random.nextDouble(1.0))}",
                                    )
                                    HMSpacerWeightRow()
                                    Text(
                                        text = "${x}b/${y}b",
                                    )
                                }
                            }
                        }
                        if (index != fileList.lastIndex) {
                            HorizontalLine()
                        }
                    }
                }

                HMSpacerWeightColumn()

                Row {
                    FittoniaButton(
                        modifier = Modifier.weight(1f),
                        onClick = { /*TODO*/ },
                    ) {
                        ButtonText(text = "Queue \uD83D\uDD03")
                    }
                    HMSpacerWidth(width = 5)
                    FittoniaButton(
                        modifier = Modifier.weight(1f),
                        onClick = { /*TODO*/ },
                    ) {
                        ButtonText(text = "Cancel ❌")
                    }
                }
                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                ) {
                    ButtonText(text = "Pause ⏸\uFE0F")
                }
            }
        },
    )
}
