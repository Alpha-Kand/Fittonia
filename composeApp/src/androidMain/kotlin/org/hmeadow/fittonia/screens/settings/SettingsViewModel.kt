package org.hmeadow.fittonia.screens.settings

import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainViewModel
import org.hmeadow.fittonia.compose.components.InputFlow

class SettingsScreenViewModel(
    private val mainViewModel: MainViewModel,
) : BaseViewModel() {
    val serverAccessCodeState = InputFlow(initial = "")

    fun onUpdateAccessCode() {
        mainViewModel.updateServerAccessCode(serverAccessCodeState.text)
    }
}
