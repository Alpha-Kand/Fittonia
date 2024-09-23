package org.hmeadow.fittonia.screens.overviewScreen

import SettingsManager
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.UserAlert
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
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.text.NumberFormat
import java.util.Locale
import java.util.Objects
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
    val needDescription: Boolean,
) {
    val totalItems = items.size
    val progressPercentage: Double =
        items.sumOf { it.progressBytes }.toDouble() / items.sumOf { it.sizeBytes }.toDouble()
    val nextItem: Int = min(currentItem + 1, totalItems)

    data class Item(
        val name: String,
        val uri: Uri,
        val isFile: Boolean,
        val sizeBytes: Long,
        val progressBytes: Long = 0,
    ) {
        val id: Int = Objects.hash(name, uri, isFile, sizeBytes) // Ignore 'progressBytes'.
    }

    enum class Direction {
        INCOMING,
        OUTGOING,
    }

    fun updateItem(item: Item): TransferJob {
        return this.copy(
            items = items.filter {
                it.id != item.id
            }.plus(item),
        )
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

class OverviewScreenViewModel(
    private val onUpdateDumpPath: (Uri) -> Unit,
) : BaseViewModel() {
    val needDumpAccess = MutableStateFlow(false)

    init {
        launch {
            when (val state = MainActivity.mainActivity.createDumpDirectory(jobName = "ACCESS")) {
                is MainActivity.CreateDumpDirectory.Error -> {
                    needDumpAccess.update { true }
                    MainActivity.mainActivity.alert(UserAlert.DumpLocationLost)
                }

                is MainActivity.CreateDumpDirectory.Success -> MainActivity.mainActivity.deleteDumpDirectory(state.uri)
            }
        }
    }

    fun onDumpPathPicked(path: Uri) {
        onUpdateDumpPath(path)
        needDumpAccess.update { false }
        MainActivity.mainActivity.unAlert<UserAlert.DumpLocationLost>()
    }
}

@Composable
fun OverviewScreen(
    viewModel: OverviewScreenViewModel,
    onSendFilesClicked: () -> Unit,
    onTransferJobClicked: (TransferJob) -> Unit,
    onAlertsClicked: () -> Unit,
) {
    var optionsState by remember { mutableStateOf(false) }
    var aboutState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = "Ongoing transfers",
                onOptionsClicked = { optionsState = true },
                onAlertsClicked = onAlertsClicked.takeIf {
                    UserAlert.hasAlerts.collectAsState(false).value
                },
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
                state = viewModel.needDumpAccess.collectAsState(initial = false).value,
                onDismiss = { viewModel.needDumpAccess.update { false } },
            ) { _ ->
                Column(modifier = Modifier.padding(all = 16.dp)) {
                    Row {
                        FittoniaIcon(
                            drawableRes = R.drawable.ic_access_folder,
                            modifier = Modifier.requiredSize(40.dp),
                            tint = Color(0xFFA00000),
                        )
                        HMSpacerWidth(width = 10)
                        Text(
                            modifier = Modifier.padding(top = 13.dp),
                            text = stringResource(R.string.overview_screen_dump_permission_lost_notice),
                            style = paragraphStyle,
                        )
                    }
                    HMSpacerHeight(height = 12)
                    FittoniaButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { MainActivity.mainActivity.openFolderPicker(viewModel::onDumpPathPicked) },
                    ) {
                        ButtonText(text = "Pick folder")
                    }
                }
            }
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
        viewModel = OverviewScreenViewModel(onUpdateDumpPath = {}),
        onSendFilesClicked = {},
        onTransferJobClicked = {},
        onAlertsClicked = {},
    )
}
