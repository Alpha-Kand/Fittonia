package org.hmeadow.fittonia

import SettingsManager
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.androidServer.AndroidServer.Companion.serverLog
import kotlin.coroutines.CoroutineContext

class MainViewModel(val dataStore: DataStore<SettingsDataAndroid>) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        println("BaseViewModel error: ${throwable.message}") // TODO - handle errors, crashlytics? before release
    }

    fun attemptAndroidServerWithPort(initServer: (port: Int, accessCode: String) -> Unit) = launch {
        serverLog(text = "attemptAndroidServerWithPort")
        dataStore.data.collect { data ->
            serverLog(text = "attemptAndroidServerWithPort data loaded")
            data.temporaryPort?.let { port ->
                serverLog(text = "attemptAndroidServerWithPort temp port loaded ($port)")
                data.serverAccessCode?.let { accessCode ->
                    serverLog(text = "attemptAndroidServerWithPort server access code loaded ($accessCode)")
                    if (port in 1025..59999) {
                        initServer(port, accessCode)
                    }
                }
            } ?: data.defaultPort.let { port ->
                serverLog(text = "attemptAndroidServerWithPort default port loaded ($port)")
                data.serverAccessCode?.let { accessCode ->
                    serverLog(text = "attemptAndroidServerWithPort server access code loaded ($accessCode)")
                    if (port in 1025..59999) {
                        initServer(port, accessCode)
                    }
                }
            }
        }
        updateTemporaryPort(port = null)
    }

    fun updateServerAccessCode(accessCode: String) = launch {
        dataStore.updateData {
            it.copy(serverAccessCode = accessCode)
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
