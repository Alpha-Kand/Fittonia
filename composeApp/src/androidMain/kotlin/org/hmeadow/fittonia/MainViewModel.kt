package org.hmeadow.fittonia

import SettingsManager
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.androidServer.AndroidServer.Companion.serverLog
import kotlin.coroutines.CoroutineContext

class MainViewModel(val dataStore: DataStore<SettingsDataAndroid>) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    fun attemptAndroidServerWithPort(initServer: (port: Int, password: String) -> Unit) = launch {
        serverLog(text = "attemptAndroidServerWithPort")
        dataStore.data.collect { data ->
            serverLog(text = "attemptAndroidServerWithPort data loaded")
            data.temporaryPort?.let { port ->
                serverLog(text = "attemptAndroidServerWithPort temp port loaded ($port)")
                data.serverPassword?.let { password ->
                    serverLog(text = "attemptAndroidServerWithPort server password loaded ($password)")
                    if (port in 1025..59999) {
                        initServer(port, password)
                    }
                }
            } ?: data.defaultPort.let { port ->
                serverLog(text = "attemptAndroidServerWithPort default port loaded ($port)")
                data.serverPassword?.let { password ->
                    serverLog(text = "attemptAndroidServerWithPort server password loaded ($password)")
                    if (port in 1025..59999) {
                        initServer(port, password)
                    }
                }
            }
        }
        updateTemporaryPort(port = null)
    }

    fun updateServerPassword(password: String) = launch {
        dataStore.updateData {
            it.copy(serverPassword = password)
        }
    }

    suspend fun updateServerPort(port: Int) {
        dataStore.updateData {
            it.copy(defaultPort = port)
        }
    }

    fun updateTemporaryPort(port: Int?) = launch {
        dataStore.updateData {
            it.copy(temporaryPort = port)
        }
    }

    fun clearDumpPath() {
        launch {
            dataStore.updateData {
                it.copy(dumpPath = SettingsDataAndroid.DumpPath())
            }
            MainActivity.mainActivity.alert(UserAlert.DumpLocationLost)
        }
    }

    fun updateDumpPath(uri: Uri) = launch {
        dataStore.updateData {
            it.copy(dumpPath = SettingsDataAndroid.DumpPath(uri = uri))
        }
    }

    fun addDestination(destination: SettingsManager.Destination) = launch {
        dataStore.updateData {
            it.copy(destinations = it.destinations + destination)
        }
    }

    fun removeDestination(destination: SettingsManager.Destination) = launch {
        dataStore.updateData { data ->
            data.copy(destinations = data.destinations.filter { it != destination })
        }
    }

    fun resetSettings() = launch {
        dataStore.updateData {
            SettingsDataAndroid()
        }
    }
}
