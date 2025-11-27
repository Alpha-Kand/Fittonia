package org.hmeadow.fittonia.screens.welcomeScreen

import android.net.Uri
import kotlinx.coroutines.flow.combine
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.mainActivity.MainViewModel

internal class WelcomeScreenViewModel(
    private val mainViewModel: MainViewModel,
    private val onContinueCallback: (accessCode: String, port: Int) -> Unit,
) : BaseViewModel() {
    val serverAccessCodeState = initInputFlow(initial = "")

    val canContinue = combine(
        serverAccessCodeState,
        mainViewModel.dataStore.data,
    ) { accessCodeState, dumpPathState ->
        accessCodeState.isNotEmpty() && dumpPathState.dumpPath.dumpUriPath.isNotEmpty()
    }

    fun onContinue() {
        onContinueCallback(
            serverAccessCodeState.text,
            44556, // TODO - after release somewhere to put constants.
        )
    }

    fun onDumpPathPicked(path: Uri) {
        mainViewModel.updateDumpPath(path)
    }
}
