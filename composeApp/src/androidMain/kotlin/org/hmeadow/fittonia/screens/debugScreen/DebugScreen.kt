package org.hmeadow.fittonia.screens.debugScreen

import SettingsManager
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.MainViewModel
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.InputFlow
import org.hmeadow.fittonia.components.pager.LazyPager
import org.hmeadow.fittonia.components.pager.PagerState
import org.hmeadow.fittonia.components.pager.PagerState.Companion.rememberPagerState
import org.hmeadow.fittonia.components.pager.PagerTabLabels
import org.hmeadow.fittonia.utility.rememberSuspendedAction
import kotlin.math.abs
import kotlin.random.Random

class DebugScreenViewModel(
    private val mainViewModel: MainViewModel,
) : BaseViewModel() {
    val deviceIp = MutableStateFlow("Unknown")

    // Defaults
    val defaultNewDestinationName = InputFlow(initial = "")
    val defaultNewDestinationPort = InputFlow(initial = "")
    val defaultNewDestinationPassword = InputFlow(initial = "")
    val defaultNewDestinationIP = InputFlow(initial = "")
    val needToSave = combine(
        mainViewModel.dataStore.data,
        defaultNewDestinationPort,
    ) { data, newDefaultDestinationPort ->
        newDefaultDestinationPort.text != data.debugSettings.defaultNewDestinationPort.toString()
    }

    // Admin Create
    val nextAutoJobName = mainViewModel.dataStore.data.map { it.nextAutoJobName }
    val nextAutoJobNameMessage = MutableStateFlow("")

    init {
        launch {
            mainViewModel.dataStore.data.first().run {
                defaultNewDestinationName.string = debugSettings.defaultNewDestinationName
                defaultNewDestinationPort.string = debugSettings.defaultNewDestinationPort.toString()
                defaultNewDestinationIP.string = debugSettings.defaultNewDestinationIP
                defaultNewDestinationPassword.string = debugSettings.defaultNewDestinationPassword
            }
        }
        refreshIp()
    }

    fun refreshIp() {
        deviceIp.value = MainActivity.mainActivity.getDeviceIpAddress() ?: "Unknown"
    }

    fun createJobDirectory() {
        launch {
            val expectedJobNumber = nextAutoJobName.first()
            when (MainActivity.mainActivity.createJobDirectory(jobName = null, print = ::println)) {
                is MainActivity.CreateDumpDirectory.Success -> {
                    nextAutoJobNameMessage.update {
                        nextAutoJobName.first().let { nextAutoJobName ->
                            if (nextAutoJobName != expectedJobNumber + 1) {
                                "Success! Job$expectedJobNumber already existed so created Job${nextAutoJobName - 1} instead."
                            } else {
                                "Success! Created Job${nextAutoJobName - 1}"
                            }
                        }
                    }
                }

                else -> nextAutoJobNameMessage.update { "Error!" }
            }
        }
    }

    fun createNewDestination() {
        val getIpNum = { abs(Random.nextInt() % 256) }
        mainViewModel.addDestination(
            destination = SettingsManager.Destination(
                name = "${defaultNewDestinationName.value} ${abs(Random.nextInt() % 100)}",
                ip = "${getIpNum()}.${getIpNum()}.${getIpNum()}.${getIpNum()}",
                password = defaultNewDestinationPassword.string,
            ),
        )
    }

    fun onSaveDefaults() {
        launch {
            mainViewModel.dataStore.updateData {
                it.copy(
                    debugSettings = it.debugSettings.copy(
                        defaultNewDestinationPort = defaultNewDestinationPort.string.toIntOrNull()
                            ?: it.debugSettings.defaultNewDestinationPort,
                    ),
                )
            }
        }
    }
}

@Composable
fun DebugScreen(
    viewModel: DebugScreenViewModel,
    data: SettingsDataAndroid,
    onResetSettingsClicked: () -> Unit,
    onClearDumpPath: () -> Unit,
    onBackClicked: () -> Unit,
    debugNewThread: suspend () -> Unit,
    debugNewDestination: () -> Unit,
    pagerState: PagerState = rememberPagerState(),
) {
    var debugAlertsState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        scrollable = false,
        header = {
            FittoniaHeader(
                headerText = "Debug Screen",
                onBackClicked = onBackClicked,
            )
        },
        content = { footerHeight ->
            val debugTabs: List<Pair<String, @Composable (maxWidth: Dp, maxHeight: Dp) -> Unit>> =
                listOf(
                    "Overview" to { maxWidth, maxHeight ->
                        DebugScreenOverviewTab(
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            viewModel = viewModel,
                            data = data,
                            onClearDumpPath = onClearDumpPath,
                            footerHeight = footerHeight,
                        )
                    },
                    "Defaults" to { maxWidth, maxHeight ->
                        DebugScreenDefaultsTab(
                            defaultNewDestinationName = viewModel.defaultNewDestinationName,
                            defaultNewDestinationPort = viewModel.defaultNewDestinationPort,
                            defaultNewDestinationPassword = viewModel.defaultNewDestinationPassword,
                            defaultNewDestinationIP = viewModel.defaultNewDestinationIP,
                            onSaveDefaults = viewModel::onSaveDefaults,
                            needToSave = viewModel.needToSave.collectAsState(initial = false).value,
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            footerHeight = footerHeight,
                        )
                    },
                    "Admin Create" to { maxWidth, maxHeight ->
                        DebugScreenAdminCreateTab(
                            nextAutoJobName = viewModel.nextAutoJobName.collectAsState(initial = -1).value,
                            nextAutoJobNameMessage = viewModel.nextAutoJobNameMessage.collectAsState(initial = "").value,
                            onCreateNewDestination = viewModel::createNewDestination,
                            onCreateJobDirectory = viewModel::createJobDirectory,
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            footerHeight = footerHeight,
                        )
                    },
                )

            BoxWithConstraints {
                val box = this
                Column {
                    PagerTabLabels(
                        position = pagerState.pagePosition,
                        tabs = remember(debugTabs) { debugTabs.map { it.first } },
                        onTabSelected = pagerState::goToPage,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                    LazyPager(
                        pagerState = pagerState,
                        spacing = 16.dp,
                    ) {
                        items(debugTabs) {
                            it.second.invoke(box.maxWidth, box.maxHeight)
                        }
                    }
                }
            }
        },
        footer = {
            Footer {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FittoniaButton(onClick = viewModel.rememberSuspendedAction(debugNewThread)) {
                            ButtonText(text = "New Thread")
                        }
                        HMSpacerWeightRow()
                        FittoniaButton(onClick = debugNewDestination) {
                            ButtonText(text = "New Destination")
                        }
                    }
                    FittoniaButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onResetSettingsClicked,
                    ) {
                        ButtonText(text = "Reset Settings")
                    }
                }
            }
        },
        overlay = {
            FittoniaModal(
                state = debugAlertsState,
                onDismiss = { debugAlertsState = false },
            ) { _ ->
                Column {
                    FittoniaButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            MainActivity.mainActivityForServer?.alert(UserAlert.PortInUse(port = 42069))
                        },
                    ) {
                        ButtonIcon(drawableRes = R.drawable.ic_add)
                        HMSpacerWidth(width = 5)
                        ButtonText(text = "UserAlert.PortInUse")
                    }
                }
            }
        },
    )
}
