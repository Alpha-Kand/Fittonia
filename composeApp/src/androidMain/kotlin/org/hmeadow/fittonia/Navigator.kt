package org.hmeadow.fittonia

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
import org.hmeadow.fittonia.screens.DebugScreen
import org.hmeadow.fittonia.screens.OverviewScreen
import org.hmeadow.fittonia.screens.SendFilesScreen
import org.hmeadow.fittonia.screens.TransferDetailsScreen
import org.hmeadow.fittonia.screens.WelcomeScreen
import org.hmeadow.fittonia.screens.WelcomeScreenViewModel

class Navigator(private val viewModelMain: MainViewModel) {

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
            mainViewModel = viewModelMain,
            onContinueCallback = { password, port ->
                viewModelMain.updateServerPassword(password)
                viewModelMain.updateServerPort(port)
                push(overviewScreen())
            },
        ),
    ) { data, viewModel ->
        WelcomeScreen(
            viewModel = viewModel,
            data = data,
            onClearDumpPath = { this.viewModelMain.updateDumpPath("") },
        )
    }

    class OverviewScreenViewModel : BaseViewModel

    private fun overviewScreen() = Screen(viewModel = OverviewScreenViewModel()) { data, viewModel ->
        OverviewScreen(
            sendFiles = {
                push(sendFilesScreen())
            },
        )
    }

    class SendFilesScreenViewModel : BaseViewModel

    private fun sendFilesScreen() = Screen(viewModel = SendFilesScreenViewModel()) { data, viewModel ->
        SendFilesScreen(
            onBackClicked = ::pop,
            onConfirmClicked = ::pop,
        )
    }

    class TransferDetailsScreenViewModel : BaseViewModel

    private val transferDetailsScreen = Screen(viewModel = TransferDetailsScreenViewModel()) { data, viewModel ->
        TransferDetailsScreen()
    }

    // TODO default splash screen?
    private var currentScreen by mutableStateOf<Screen<out BaseViewModel>>(loadingScreen())
    private val screenStack = mutableListOf<Screen<out BaseViewModel>>()

    init {
        instance = this
        viewModelMain.launch {
            viewModelMain.dataStore.data.first().let {
                if (it.defaultPort != 0 && it.serverPassword != null) {
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

        private class DebugScreenViewModel : BaseViewModel

        fun goToDebugScreen() {
            instance.push(
                Screen(viewModel = DebugScreenViewModel()) { data, _ ->
                    DebugScreen(
                        data = data,
                        onResetSettingsClicked = instance.viewModelMain::resetSettings,
                        onBackClicked = instance::pop,
                    )
                },
            )
        }
    }
}
