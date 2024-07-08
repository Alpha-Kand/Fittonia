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
import org.hmeadow.fittonia.screens.MainScreen
import org.hmeadow.fittonia.screens.SendFilesScreen
import org.hmeadow.fittonia.screens.TransferDetailsScreen
import org.hmeadow.fittonia.screens.WelcomeScreen

class Navigator(private val viewModel: MainViewModel) {

    data class Screen(
        val compose: @Composable (SettingsDataAndroid, MainViewModel) -> Unit,
    )

    private val loadingScreen = Screen { _, _ ->
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

    private val welcomeScreen = Screen { data, viewModel ->
        WelcomeScreen(
            data = data,
            onContinue = { password, port ->
                viewModel.updateServerPassword(password)
                viewModel.updateServerPort(port)
                push(mainScreen)
            },
        )
    }

    private val mainScreen = Screen { data, viewModel ->
        MainScreen(
            sendFiles = {
                push(sendFilesScreen)
            },
        )
    }

    private val sendFilesScreen = Screen { data, viewModel ->
        SendFilesScreen(
            onBackClicked = {
                pop()
            },
            onConfirmClicked = {
                pop()
            },
        )
    }

    private val transferDetailsScreen = Screen { data, viewModel ->
        TransferDetailsScreen()
    }

    private var currentScreen by mutableStateOf(loadingScreen) // TODO default splash screen?
    private val screenStack = mutableListOf<Screen>()

    init {
        instance = this
        viewModel.launch {
            viewModel.dataStore.data.first().let {
                if (it.defaultPort != 0 && it.serverPassword != null) {
                    push(mainScreen)
                } else {
                    push(welcomeScreen)
                }
            }
        }

        screenStack.add(welcomeScreen)
    }

    private fun push(screen: Screen) {
        screenStack.add(screen)
        currentScreen = screen
    }

    private fun pop() {
        screenStack.removeLast()
        currentScreen = screenStack.last()
    }

    @Composable
    fun Render(settingsDataAndroid: SettingsDataAndroid, viewModel: MainViewModel) {
        currentScreen.compose.invoke(settingsDataAndroid, viewModel)
    }

    companion object {
        private lateinit var instance: Navigator
        fun goToDebugScreen() {
            instance.push(
                Screen { data, _ ->
                    DebugScreen(
                        data = data,
                        onBackClicked = instance::pop,
                    )
                },
            )
        }
    }
}
