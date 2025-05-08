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
import org.hmeadow.fittonia.compose.architecture.Debug
import kotlin.coroutines.CoroutineContext

class MainViewModel(val dataStore: DataStore<SettingsDataAndroid>) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        println("BaseViewModel error: ${throwable.message}") // TODO - handle errors, crashlytics? before release
    }

    init {
        launch {
            dataStore.data.collect {
                it.debugSettings.colourSettings.run {
                    Debug.statusBarColourEdit = statusBarColour.unserialize
                    Debug.headerBackgroundColourEdit = headerBackgroundColour.unserialize
                    Debug.footerBackgroundColourEdit = footerBackgroundColour.unserialize
                    Debug.backgroundColourEdit = backgroundColour.unserialize

                    Debug.readOnlyBackgroundColourEdit = readOnlyBackgroundColour.unserialize
                    Debug.readOnlyBorderColourEdit = readOnlyBorderColour.unserialize
                    Debug.readOnlyClearIconColourEdit = readOnlyClearIconColour.unserialize

                    debugPrimaryButtonColours.run {
                        Debug.primaryButtonBorderColour = primaryButtonBorderColour.unserialize
                        Debug.primaryButtonContentColour = primaryButtonContentColour.unserialize
                        Debug.primaryButtonBackgroundColour = primaryButtonBackgroundColour.unserialize
                        Debug.primaryButtonDisabledBorderColour = primaryButtonDisabledBorderColour.unserialize
                        Debug.primaryButtonDisabledContentColour = primaryButtonDisabledContentColour.unserialize
                        Debug.primaryButtonDisabledBackgroundColour = primaryButtonDisabledBackgroundColour.unserialize
                    }

                    debugSecondaryButtonColours.run {
                        Debug.secondaryButtonBorderColour = secondaryButtonBorderColour.unserialize
                        Debug.secondaryButtonContentColour = secondaryButtonContentColour.unserialize
                        Debug.secondaryButtonBackgroundColour = secondaryButtonBackgroundColour.unserialize
                        Debug.secondaryButtonDisabledBorderColour = secondaryButtonDisabledBorderColour.unserialize
                        Debug.secondaryButtonDisabledContentColour = secondaryButtonDisabledContentColour.unserialize
                        Debug.secondaryButtonDisabledBackgroundColour =
                            secondaryButtonDisabledBackgroundColour.unserialize
                    }
                }
            }
        }
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

    fun saveColours() = launch {
        dataStore.updateData { data ->
            data.copy(
                debugSettings = data.debugSettings.copy(
                    colourSettings = SettingsDataAndroid.DebugAppStyle(
                        statusBarColour = Debug.statusBarColourEdit.serialize,
                        headerBackgroundColour = Debug.headerBackgroundColourEdit.serialize,
                        footerBackgroundColour = Debug.footerBackgroundColourEdit.serialize,
                        backgroundColour = Debug.backgroundColourEdit.serialize,
                        readOnlyBorderColour = Debug.readOnlyBorderColourEdit.serialize,
                        readOnlyBackgroundColour = Debug.readOnlyBackgroundColourEdit.serialize,
                        readOnlyClearIconColour = Debug.readOnlyClearIconColourEdit.serialize,
                        debugPrimaryButtonColours = SettingsDataAndroid.DebugPrimaryButtonColours(
                            primaryButtonBorderColour = Debug.primaryButtonBorderColour.serialize,
                            primaryButtonContentColour = Debug.primaryButtonContentColour.serialize,
                            primaryButtonBackgroundColour = Debug.primaryButtonBackgroundColour.serialize,
                            primaryButtonDisabledBorderColour = Debug.primaryButtonDisabledBorderColour.serialize,
                            primaryButtonDisabledContentColour = Debug.primaryButtonDisabledContentColour.serialize,
                            primaryButtonDisabledBackgroundColour = Debug.primaryButtonDisabledBackgroundColour.serialize,
                        ),
                        debugSecondaryButtonColours = SettingsDataAndroid.DebugSecondaryButtonColours(
                            secondaryButtonBorderColour = Debug.secondaryButtonBorderColour.serialize,
                            secondaryButtonContentColour = Debug.secondaryButtonContentColour.serialize,
                            secondaryButtonBackgroundColour = Debug.secondaryButtonBackgroundColour.serialize,
                            secondaryButtonDisabledBorderColour = Debug.secondaryButtonDisabledBorderColour.serialize,
                            secondaryButtonDisabledContentColour = Debug.secondaryButtonDisabledContentColour.serialize,
                            secondaryButtonDisabledBackgroundColour = Debug.secondaryButtonDisabledBackgroundColour.serialize,
                        ),
                    ),
                ),
            )
        }
    }
}
