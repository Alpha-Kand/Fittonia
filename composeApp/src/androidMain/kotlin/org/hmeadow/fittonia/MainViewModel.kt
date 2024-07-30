package org.hmeadow.fittonia

import SettingsManager
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainViewModel(val dataStore: DataStore<SettingsDataAndroid>) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    fun attemptAndroidServerWithPort(initServer: (port: Int, password: String) -> Unit) = launch {
        dataStore.data.collect { data ->
            data.temporaryPort?.let { port ->
                data.serverPassword?.let { password ->
                    if (port in 1025..49999) {
                        initServer(port, password)
                    }
                }
            } ?: data.defaultPort.let { port ->
                data.serverPassword?.let { password ->
                    if (port in 1025..49999) {
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

    fun updateServerPort(port: Int) = launch {
        dataStore.updateData {
            it.copy(defaultPort = port)
        }
    }

    fun updateTemporaryPort(port: Int?) = launch {
        dataStore.updateData {
            it.copy(temporaryPort = port)
        }
    }

    fun clearDumpPath() = launch {
        dataStore.updateData {
            it.copy(dumpPath = SettingsDataAndroid.DumpPath())
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
