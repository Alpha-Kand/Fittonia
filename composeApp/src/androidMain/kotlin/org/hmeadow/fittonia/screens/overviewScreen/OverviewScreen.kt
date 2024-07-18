package org.hmeadow.fittonia.screens.overviewScreen

import SettingsManager
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaComingSoon
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.design.fonts.paragraphStyle
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

@Composable
fun rememberPercentageFormat(
    percentage: Double,
    minFraction: Int? = null,
    maxFraction: Int? = 2,
): String = remember(
    percentage,
    minFraction,
    maxFraction,
) {
    NumberFormat.getPercentInstance(Locale.getDefault()).apply {
        maxFraction?.let { maximumFractionDigits = maxFraction }
        minFraction?.let { minimumFractionDigits = minFraction }
    }.format(percentage)
}

class Options(
    val name: String,
    val onClick: () -> Unit,
)

data class TransferJob(
    val id: Int,
    val description: String,
    val destination: SettingsManager.Destination,
    val items: List<Item>,
    val currentItem: Int = 1,
    val port: Int,
    val status: TransferStatus,
    val direction: Direction,
) {
    val totalItems = items.size
    val progressPercentage: Double = currentItem / totalItems.toDouble()
    val nextItem: Int = min(currentItem + 1, totalItems)

    data class Item(
        val name: String,
        val uri: Uri,
    )

    enum class Direction {
        INCOMING,
        OUTGOING,
    }
}

enum class TransferStatus {
    Sending,
    Receiving,
    Error,
    Done,
}

@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

@Composable
fun OverviewScreen(
    onSendFilesClicked: () -> Unit,
    onTransferJobClicked: (TransferJob) -> Unit,
) {
    var optionsState by remember { mutableStateOf(false) }
    var aboutState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = "Ongoing transfers",
                onOptionsClicked = { optionsState = true },
            )
        },
        content = {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                HMSpacerHeight(height = 15)

                TransferList(onTransferJobClicked = onTransferJobClicked)
            }
        },
        footer = {
            Footer {
                Row {
                    FittoniaButton(
                        modifier = Modifier.weight(1.0f),
                        onClick = onSendFilesClicked,
                        content = { ButtonText(text = "Send files") },
                    )
                    HMSpacerWidth(20)
                    FittoniaComingSoon(
                        modifier = Modifier.weight(1.0f),
                    ) {
                        FittoniaButton(
                            onClick = { /*TODO*/ },
                            content = { ButtonText(text = "Send Message") },
                        )
                    }
                }
            }
        },
        overlay = {
            FittoniaModal(
                state = aboutState,
                onDismiss = { aboutState = false },
            ) { _ ->
                Text(
                    text = "Made by me :3",
                )
            }
            FittoniaModal(
                state = optionsState,
                onDismiss = { optionsState = false },
            ) { onDismiss ->
                listOf(
                    Options(
                        name = "Settings",
                        onClick = {},
                    ),
                    Options(
                        name = "About",
                        onClick = {
                            aboutState = true
                        },
                    ),
                ).forEach {
                    if (it.name == "Settings") {
                        FittoniaComingSoon {
                            Row {
                                Text(
                                    text = it.name,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    style = paragraphStyle,
                                )
                                HMSpacerWeightRow()
                                FittoniaIcon(
                                    modifier = Modifier.align(CenterVertically),
                                    drawableRes = R.drawable.ic_chevron_right,
                                    tint = Color(0xFF222222),
                                )
                            }
                        }
                    } else {
                        Row {
                            Text(
                                text = it.name,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                                    .clickable(
                                        onClick = {
                                            onDismiss()
                                            it.onClick()
                                        },
                                    ),
                                style = paragraphStyle,
                            )
                            HMSpacerWeightRow()
                            FittoniaIcon(
                                modifier = Modifier.align(CenterVertically),
                                drawableRes = R.drawable.ic_chevron_right,
                                tint = Color(0xFF222222),
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
@Preview
private fun Preview() {
    OverviewScreen(
        onSendFilesClicked = {},
        onTransferJobClicked = {},
    )
}
