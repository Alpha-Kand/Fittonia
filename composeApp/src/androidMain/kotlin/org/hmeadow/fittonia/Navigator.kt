package org.hmeadow.fittonia

import SettingsManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.androidServer.AndroidServer
import org.hmeadow.fittonia.androidServer.AndroidServer.Companion.server
import org.hmeadow.fittonia.androidServer.AndroidServer.Companion.startSending
import org.hmeadow.fittonia.compose.architecture.dataState
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.screens.AlertsScreen
import org.hmeadow.fittonia.screens.AlertsScreenViewModel
import org.hmeadow.fittonia.screens.NewDestinationScreen
import org.hmeadow.fittonia.screens.NewDestinationScreenViewModel
import org.hmeadow.fittonia.screens.SendFilesScreen
import org.hmeadow.fittonia.screens.SendFilesScreenViewModel
import org.hmeadow.fittonia.screens.TransferDetailsScreen
import org.hmeadow.fittonia.screens.TransferDetailsScreenViewModel
import org.hmeadow.fittonia.screens.WelcomeScreen
import org.hmeadow.fittonia.screens.WelcomeScreenViewModel
import org.hmeadow.fittonia.screens.debugScreen.DebugScreen
import org.hmeadow.fittonia.screens.debugScreen.DebugScreenViewModel
import org.hmeadow.fittonia.screens.overviewScreen.OverviewScreen
import org.hmeadow.fittonia.screens.overviewScreen.OverviewScreenViewModel
import org.hmeadow.fittonia.screens.settings.SettingsScreen
import org.hmeadow.fittonia.screens.settings.SettingsScreenViewModel
import kotlin.math.abs
import kotlin.random.Random

class Navigator(private val mainViewModel: MainViewModel) {

    data class Screen<T : BaseViewModel>(
        private val viewModel: T,
        private val compose: @Composable (SettingsDataAndroid, T) -> Unit,
    ) {
        @Composable
        fun Render(data: SettingsDataAndroid) {
            compose(data, viewModel)
        }
    }

    class LoadingScreenViewModel : BaseViewModel()

    private fun loadingScreen() = Screen(
        viewModel = LoadingScreenViewModel(),
    ) { _, _ ->
        Box(
            modifier = Modifier
                .background(Color.Cyan)
                .fillMaxSize(),
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Loading...",
            )
        }
    }

    private fun welcomeScreen() = Screen(
        viewModel = WelcomeScreenViewModel(
            mainViewModel = mainViewModel,
            onContinueCallback = { accessCode, port ->
                mainViewModel.updateServerAccessCode(accessCode)
                mainViewModel.launch {
                    mainViewModel.updateServerPort(port)
                }
                MainActivity.mainActivity.attemptStartServer()
                push(overviewScreen())
            },
        ),
    ) { data, viewModel ->
        WelcomeScreen(
            viewModel = viewModel,
            data = data,
            onClearDumpPath = this.mainViewModel::clearDumpPath,
        )
    }

    private fun overviewScreen() = Screen(
        viewModel = OverviewScreenViewModel(
            onUpdateDumpPath = mainViewModel::updateDumpPath,
        ),
    ) { data, viewModel ->
        OverviewScreen(
            viewModel = viewModel,
            data = data,
            onSendFilesClicked = {
                push(sendFilesScreen())
            },
            onTransferJobClicked = { job ->
                push(transferDetailsScreen(transferJob = job))
            },
            onAlertsClicked = {
                push(alertsScreen())
            },
            onGoToSettingsClicked = {
                push(settingsScreen())
            },
        )
    }

    private fun sendFilesScreen() = Screen(
        viewModel = SendFilesScreenViewModel(
            onSaveOneTimeDestinationCallback = { oneTimeIp, oneTimeAccessCode, onFinish ->
                push(
                    newDestinationScreen(
                        oneTimeIp = oneTimeIp,
                        oneTimeAccessCode = oneTimeAccessCode,
                        onFinish = onFinish,
                    ),
                )
            },
            onAddNewDestinationCallback = { onFinish ->
                push(newDestinationScreen(onFinish = onFinish))
            },
            onPing = AndroidServer::ping,
            onConfirmCallback = { newJob ->
                startSending(newJob = newJob)
                pop()
            },
        ),
    ) { data, viewModel ->
        SendFilesScreen(
            viewModel = viewModel,
            data = data,
            onBackClicked = ::pop,
        )
    }

    private fun newDestinationScreen(
        oneTimeIp: String? = null,
        oneTimeAccessCode: String? = null,
        onFinish: (SettingsManager.Destination) -> Unit,
    ) = Screen(
        viewModel = NewDestinationScreenViewModel(
            oneTimeIp = oneTimeIp,
            oneTimeAccessCode = oneTimeAccessCode,
            onSaveNewDestinationCallback = { newDestination ->
                mainViewModel.addDestination(newDestination)
                onFinish(newDestination)
                pop()
            },
        ),
    ) { _, viewModel ->
        NewDestinationScreen(
            viewModel = viewModel,
            onBackClicked = ::pop,
        )
    }

    private fun transferDetailsScreen(
        transferJob: TransferJob,
    ) = Screen(viewModel = TransferDetailsScreenViewModel(transferJob = transferJob)) { _, viewModel ->
        TransferDetailsScreen(
            transferJobState = viewModel.currentTransferJob.dataState,
            onBackClicked = ::pop,
        )
    }

    private fun settingsScreen() = Screen(
        viewModel = SettingsScreenViewModel(mainViewModel = mainViewModel),
    ) { data, viewModel ->
        SettingsScreen(
            viewModel = viewModel,
            data = data,
            onBackClicked = ::pop,
        )
    }

    private fun alertsScreen() = Screen(
        viewModel = AlertsScreenViewModel(
            onUpdateDumpPath = mainViewModel::updateDumpPath,
            onTemporaryPortAcceptedCallback = { newPort ->
                mainViewModel.updateTemporaryPort(port = newPort)
                MainActivity.mainActivity.unAlert<UserAlert.PortInUse>()
                println("onTemporaryPortAcceptedCallback server.value = ${server.value}")
                if (server.value == null) {
                    MainActivity.mainActivity.attemptStartServer()
                } else {
                    server.value?.restartServerSocket(port = newPort)
                }
            },
            onNewDefaultPortAcceptedCallback = { newPort ->
                mainViewModel.updateServerPort(port = newPort)
                MainActivity.mainActivity.unAlert<UserAlert.PortInUse>()
                println("onNewDefaultPortAcceptedCallback server.value = ${server.value}")
                if (server.value == null) {
                    MainActivity.mainActivity.attemptStartServer()
                } else {
                    server.value?.restartServerSocket(port = newPort)
                }
            },
        ),
    ) { _, viewModel ->
        AlertsScreen(
            viewModel = viewModel,
            onBackClicked = ::pop,
        )
    }

    // TODO FOR REAL default splash screen?
    private var currentScreen by mutableStateOf<Screen<out BaseViewModel>>(loadingScreen())
    private val screenStack = mutableListOf<Screen<out BaseViewModel>>()

    init {
        instance = this
        mainViewModel.launch {
            mainViewModel.dataStore.data.first().let {
                if (it.defaultPort != 0 && it.serverAccessCode != null && it.dumpPath.isSet) {
                    push(overviewScreen())
                } else {
                    push(welcomeScreen())
                }
            }
        }
    }

    private fun push(screen: Screen<out BaseViewModel>) {
        screenStack.add(screen)
        currentScreen = screen
    }

    private fun pop() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            currentScreen = screenStack.last()
        }
    }

    @Composable
    fun Render(settingsDataAndroid: SettingsDataAndroid) {
        currentScreen.Render(settingsDataAndroid)
    }

    companion object {
        private lateinit var instance: Navigator

        @Composable
        fun NavigatorBackHandler() {
            BackHandler(enabled = instance.screenStack.size > 1) {
                instance.pop()
            }
        }

        fun goToDebugScreen() {
            instance.push(
                Screen(
                    viewModel = DebugScreenViewModel(
                        mainViewModel = instance.mainViewModel,
                    ),
                ) { data, viewModel ->
                    DebugScreen(
                        viewModel = viewModel,
                        data = data,
                        onResetSettingsClicked = instance.mainViewModel::resetSettings,
                        onResetColours = instance.mainViewModel::resetColours,
                        onClearDumpPath = instance.mainViewModel::clearDumpPath,
                        onRemoveDestinationClicked = instance.mainViewModel::removeDestination,
                        onBackClicked = instance::pop,
                        debugNewDestination = {
                            val getIpNum = { abs(Random.nextInt() % 256) }
                            instance.mainViewModel.addDestination(
                                destination = SettingsManager.Destination(
                                    name = "Destination ${abs(Random.nextInt() % 100)}",
                                    ip = "${getIpNum()}.${getIpNum()}.${getIpNum()}.${getIpNum()}",
                                    accessCode = "accesscode",
                                ),
                            )
                        },
                        saveColours = instance.mainViewModel::saveColours,
                        cancelColourChanges = instance.mainViewModel::loadColours,
                    )
                },
            )
        }
    }
}
