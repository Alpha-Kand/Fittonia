package org.hmeadow.fittonia

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainViewModel(val dataStore: DataStore<SettingsDataAndroid>) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

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

    fun updateDumpPath(dumpPath:String) = launch {
        dataStore.updateData {
            it.copy(dumpPath = dumpPath)
        }
    }
}
