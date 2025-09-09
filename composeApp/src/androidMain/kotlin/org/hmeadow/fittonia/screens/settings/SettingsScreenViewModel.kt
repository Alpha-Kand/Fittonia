package org.hmeadow.fittonia.screens.settings

import android.net.Uri
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.mainActivity.MainViewModel

internal class SettingsScreenViewModel(
    private val mainViewModel: MainViewModel,
) : BaseViewModel() {
    val serverAccessCodeState = InputFlow(initial = "")

    fun onUpdateAccessCode() {
        mainViewModel.updateServerAccessCode(serverAccessCodeState.text)
    }

    fun onDumpPathPicked(path: Uri) {
        mainViewModel.updateDumpPath(path)
    }
}
