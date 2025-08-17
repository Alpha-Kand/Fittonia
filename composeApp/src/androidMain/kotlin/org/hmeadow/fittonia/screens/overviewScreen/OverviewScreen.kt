package org.hmeadow.fittonia.screens.overviewScreen

import SettingsManager
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.androidServer.AndroidServer
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.pager.LazyPager
import org.hmeadow.fittonia.components.pager.PagerState
import org.hmeadow.fittonia.components.pager.PagerState.Companion.rememberPagerState
import org.hmeadow.fittonia.components.pager.PagerTabLabels
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWeightRow
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingMStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus
import org.hmeadow.fittonia.utility.createJobDirectory
import org.hmeadow.fittonia.utility.isLandscape
import kotlin.math.abs
import kotlin.random.Random

class Options(
    val name: String,
    val onClick: () -> Unit,
)

class OverviewScreenViewModel(
    private val onUpdateDumpPath: (Uri) -> Unit,
) : BaseViewModel() {
    val needDumpAccess = MutableStateFlow(false)

    init {
        launch {
            when (val state = MainActivity.mainActivity.createJobDirectory(jobName = "ACCESS")) {
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

    fun addNewDebugJob() = launch {
        AndroidServer.server.value?.registerTransferJob(
            OutgoingJob(
                id = Random.nextInt(),
                description = "Sending PDFs to bob (${abs(Random.nextInt() % 100)})",
                needDescription = false,
                destination = SettingsManager.Destination(
                    name = "Bob's PC (${abs(Random.nextInt() % 100)})",
                    ip = "192.168.1.1",
                    accessCode = "accesscode",
                ),
                items = (0..abs(Random.nextInt() % 20)).map {
                    val totalSize = abs(Random.nextLong())
                    TransferJob.Item(
                        name = "File_${abs(Random.nextInt() % 100)}.pdf",
                        uriRaw = "https://www.google.com",
                        isFile = true,
                        sizeBytes = totalSize,
                        progressBytes = Random.nextLong(from = 0, until = totalSize),
                    )
                },
                port = 5556,
                status = TransferStatus.entries.random(),
            ),
        )
    }
}

@Composable
fun OverviewScreen(
    viewModel: OverviewScreenViewModel,
    data: SettingsDataAndroid,
    onSendFilesClicked: () -> Unit,
    onTransferJobClicked: (TransferJob) -> Unit,
    onAlertsClicked: () -> Unit,
    onGoToSettingsClicked: () -> Unit,
    pagerState: PagerState = rememberPagerState(),
) {
    var optionsState by remember { mutableStateOf(false) }
    var aboutState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        scrollable = false,
        header = {
            FittoniaHeader(
                onOptionsClicked = { optionsState = true },
                onAlertsClicked = onAlertsClicked.takeIf {
                    UserAlert.hasAlerts.collectAsState(false).value
                },
            )
        },
        content = {
            val overviewTabs: List<Pair<String, @Composable (maxWidth: Dp, maxHeight: Dp) -> Unit>> =
                listOf(
                    "Active transfers" to { maxWidth, maxHeight ->
                        OverviewScreenActiveTransfersTab(
                            maxWidth = maxWidth,
                            maxHeight = maxHeight,
                            onTransferJobClicked = onTransferJobClicked,
                            addNewDebugJob = viewModel::addNewDebugJob,
                        )
                    },
                    "Completed transfers" to { maxWidth, maxHeight ->
                        OverviewScreenCompletedTransfersTab(
                            maxWidth = maxWidth,
                            maxHeight = maxHeight,
                            data.completedJobs,
                        )
                    },
                )

            BoxWithConstraints {
                val box = this
                Column {
                    PagerTabLabels(
                        position = pagerState.pagePosition,
                        tabs = remember(overviewTabs) { overviewTabs.map { it.first } },
                        onTabSelected = pagerState::goToPage,
                        modifier = Modifier.padding(bottom = spacing16),
                    )
                    LazyPager(
                        pagerState = pagerState,
                        spacing = spacing16,
                    ) {
                        items(overviewTabs) {
                            it.second.invoke(box.maxWidth, box.maxHeight)
                        }
                    }
                }
            }
        },
        footer = {
            Footer {
                Row {
                    FittoniaButton(
                        modifier = Modifier.weight(weight = 1.0f),
                        onClick = onSendFilesClicked,
                        content = { ButtonText(text = "Send files") },
                    )
                    if (isLandscape()) {
                        FittoniaSpacerWeightRow(weight = 1.0f)
                    }
                }
            }
        },
        overlay = {
            FittoniaModal(
                state = viewModel.needDumpAccess.collectAsState(initial = false).value,
                onDismiss = { viewModel.needDumpAccess.update { false } },
            ) { _ ->
                Column(modifier = Modifier.padding(all = spacing16)) {
                    Row {
                        FittoniaIcon(
                            drawableRes = R.drawable.ic_access_folder,
                            modifier = Modifier.requiredSize(size = 40.dp),
                            tint = Color(color = 0xFFA00000),
                        )
                        FittoniaSpacerWidth(width = spacing8)
                        Text(
                            modifier = Modifier.padding(top = spacing16),
                            text = stringResource(R.string.overview_screen_dump_permission_lost_notice),
                            style = paragraphTextStyle,
                        )
                    }
                    FittoniaSpacerHeight(height = spacing16)
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
                Column(modifier = Modifier.padding(vertical = spacing16)) {
                    Text(
                        text = stringResource(
                            R.string.about_modal_name_format,
                            stringResource(R.string.app_name),
                            stringResource(R.string.app_version),
                        ),
                        style = headingMStyle,
                    )
                    FittoniaSpacerHeight(height = spacing8)
                    Text(
                        text = stringResource(R.string.last_built_date),
                        style = paragraphTextStyle,
                    )
                    FittoniaSpacerHeight(height = spacing8)
                    Text(
                        text = stringResource(R.string.credits),
                        style = paragraphTextStyle,
                    )
                }
            }
            FittoniaModal(
                state = optionsState,
                alignment = Alignment.TopStart,
                contentBackgroundColour = currentStyle.primaryButtonType.backgroundColor,
                contentBorderColour = currentStyle.primaryButtonType.borderColour,
                onDismiss = { optionsState = false },
            ) { onDismiss ->
                val list = listOf(
                    Options(
                        name = "Settings",
                        onClick = onGoToSettingsClicked,
                    ),
                    Options(
                        name = "About",
                        onClick = { aboutState = true },
                    ),
                )
                list.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    onDismiss()
                                    option.onClick()
                                },
                            ),
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = spacing16)
                                .padding(end = spacing16),
                            text = option.name,
                            style = paragraphTextStyle,
                            color = currentStyle.primaryButtonType.contentColour,
                        )
                        FittoniaSpacerWeightRow()
                        FittoniaIcon(
                            modifier = Modifier
                                .align(alignment = CenterVertically)
                                .requiredSize(size = spacing16),
                            drawableRes = R.drawable.ic_chevron_right,
                            tint = currentStyle.primaryButtonType.contentColour,
                        )
                    }
                    if (list.lastIndex != index) {
                        HorizontalLine(color = currentStyle.primaryButtonType.contentColour)
                    }
                }
            }
        },
    )
}
