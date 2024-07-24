package org.hmeadow.fittonia

import SettingsManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.AndroidServer.Companion.startThread
import org.hmeadow.fittonia.screens.DebugScreen
import org.hmeadow.fittonia.screens.DebugScreenViewModel
import org.hmeadow.fittonia.screens.NewDestinationScreen
import org.hmeadow.fittonia.screens.NewDestinationScreenViewModel
import org.hmeadow.fittonia.screens.SendFilesScreen
import org.hmeadow.fittonia.screens.SendFilesScreenViewModel
import org.hmeadow.fittonia.screens.TransferDetailsScreen
import org.hmeadow.fittonia.screens.WelcomeScreen
import org.hmeadow.fittonia.screens.WelcomeScreenViewModel
import org.hmeadow.fittonia.screens.overviewScreen.OverviewScreen
import org.hmeadow.fittonia.screens.overviewScreen.TransferJob
import org.hmeadow.fittonia.screens.overviewScreen.TransferStatus
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

    class LoadingScreenViewModel : BaseViewModel

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
            onContinueCallback = { password, port ->
                mainViewModel.updateServerPassword(password)
                mainViewModel.updateServerPort(port)
                push(overviewScreen())
            },
        ),
    ) { data, viewModel ->
        WelcomeScreen(
            viewModel = viewModel,
            data = data,
            onClearDumpPath = { this.mainViewModel.clearDumpPath() },
        )
    }

    class OverviewScreenViewModel : BaseViewModel

    private fun overviewScreen() = Screen(viewModel = OverviewScreenViewModel()) { data, viewModel ->
        OverviewScreen(
            onSendFilesClicked = {
                push(sendFilesScreen())
            },
            onTransferJobClicked = { job ->
                push(transferDetailsScreen(transferJob = job))
            },
        )
    }

    private fun sendFilesScreen() = Screen(
        viewModel = SendFilesScreenViewModel(
            onSaveOneTimeDestinationCallback = { oneTimeIp, oneTimePassword, onFinish ->
                push(
                    newDestinationScreen(
                        oneTimeIp = oneTimeIp,
                        oneTimePassword = oneTimePassword,
                        onFinish = onFinish,
                    ),
                )
            },
            onAddNewDestinationCallback = { onFinish ->
                push(newDestinationScreen(onFinish = onFinish))
            },
            onConfirmCallback = { newJob ->
                startThread(newJob = newJob)
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
        oneTimePassword: String? = null,
        onFinish: (SettingsManager.Destination) -> Unit,
    ) = Screen(
        viewModel = NewDestinationScreenViewModel(
            oneTimeIp = oneTimeIp,
            oneTimePassword = oneTimePassword,
            onSaveNewDestinationCallback = { newDestination ->
                mainViewModel.addDestination(newDestination)
                onFinish(newDestination)
                pop()
            },
        ),
    ) { data, viewModel ->
        NewDestinationScreen(
            viewModel = viewModel,
            onBackClicked = ::pop,
        )
    }

    class TransferDetailsScreenViewModel : BaseViewModel

    private fun transferDetailsScreen(
        transferJob: TransferJob,
    ) = Screen(viewModel = TransferDetailsScreenViewModel()) { data, viewModel ->
        TransferDetailsScreen(
            transferJob = transferJob,
            onBackClicked = ::pop,
        )
    }

    // TODO default splash screen?
    private var currentScreen by mutableStateOf<Screen<out BaseViewModel>>(loadingScreen())
    private val screenStack = mutableListOf<Screen<out BaseViewModel>>()

    init {
        instance = this
        mainViewModel.launch {
            mainViewModel.dataStore.data.first().let {
                if (it.defaultPort != 0 && it.serverPassword != null && it.dumpPath.isSet) {
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
        screenStack.removeLast()
        currentScreen = screenStack.last()
    }

    @Composable
    fun Render(settingsDataAndroid: SettingsDataAndroid) {
        currentScreen.Render(settingsDataAndroid)
    }

    companion object {
        private lateinit var instance: Navigator

        fun goToDebugScreen() {
            instance.push(
                Screen(viewModel = DebugScreenViewModel()) { data, viewModel ->
                    DebugScreen(
                        viewModel = viewModel,
                        data = data,
                        onResetSettingsClicked = instance.mainViewModel::resetSettings,
                        onClearDumpPath = instance.mainViewModel::clearDumpPath,
                        onRemoveDestinationClicked = instance.mainViewModel::removeDestination,
                        onBackClicked = instance::pop,
                        debugNewThread = {
                            startThread(
                                TransferJob(
                                    id = Random.nextInt(),
                                    description = "Sending PDFs to bob (${abs(Random.nextInt() % 100)})",
                                    destination = SettingsManager.Destination(
                                        name = "Bob's PC (${abs(Random.nextInt() % 100)})",
                                        ip = "192.168.1.1",
                                        password = "Password",
                                    ),
                                    items = (0..abs(Random.nextInt() % 100)).map {
                                        TransferJob.Item(
                                            name = "File_${abs(Random.nextInt() % 100)}.pdf",
                                            uri = Uri.parse("https://www.google.com"),
                                        )
                                    },
                                    port = 5556,
                                    status = TransferStatus.Sending,
                                    direction = TransferJob.Direction.OUTGOING,
                                ),
                            )
                        },
                        debugNewDestination = {
                            val getIpNum = { abs(Random.nextInt() % 256) }
                            instance.mainViewModel.addDestination(
                                destination = SettingsManager.Destination(
                                    name = "Destination ${abs(Random.nextInt() % 100)}",
                                    ip = "${getIpNum()}.${getIpNum()}.${getIpNum()}.${getIpNum()}",
                                    password = "Password",
                                ),
                            )
                        },
                    )
                },
            )
        }
    }
}
