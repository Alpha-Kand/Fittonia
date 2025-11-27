package org.hmeadow.fittonia.screens.alertsScreen

import android.net.Uri
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.mainActivity.MainActivity

internal class AlertsScreenViewModel(
    private val onUpdateDumpPath: (Uri) -> Unit,
    private val onTemporaryPortAcceptedCallback: suspend (port: Int) -> Unit,
    private val onNewDefaultPortAcceptedCallback: suspend (port: Int) -> Unit,
) : BaseViewModel() {
    val temporaryPort: InputFlow = initInputFlow(initial = "")
    val newDefaultPort: InputFlow = initInputFlow(initial = "")

    suspend fun onTemporaryPortAccepted() = onTemporaryPortAcceptedCallback(temporaryPort.text.toInt())
    suspend fun onNewDefaultPortAccepted() = onNewDefaultPortAcceptedCallback(newDefaultPort.text.toInt())

    fun onDumpPathPicked(path: Uri) {
        onUpdateDumpPath(path)
        MainActivity.mainActivity.unAlert<UserAlert.DumpLocationLost>()
    }
}
