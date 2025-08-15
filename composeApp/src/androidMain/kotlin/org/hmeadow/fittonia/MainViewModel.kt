package org.hmeadow.fittonia

import SettingsManager
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.androidServer.AndroidServer
import org.hmeadow.fittonia.androidServer.AndroidServer.Companion.serverLog
import org.hmeadow.fittonia.compose.architecture.DebugAppStyle
import recordThrowable
import kotlin.coroutines.CoroutineContext

class MainViewModel(val dataStore: DataStore<SettingsDataAndroid>) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
        recordThrowable(throwable = throwable)
        println("BaseViewModel error: ${throwable.message}")
    }

    init {
        loadColours()
    }

    fun loadColours() = launch {
        dataStore.data.collect {
            it.debugSettings.colourSettings.run {
                DebugAppStyle.statusBarColourEdit = statusBarColour.unserialize
                DebugAppStyle.headerBackgroundColourEdit = headerBackgroundColour.unserialize
                DebugAppStyle.footerBackgroundColourEdit = footerBackgroundColour.unserialize
                DebugAppStyle.headerAndFooterBorderColourEdit = headerFooterBorderColour.unserialize
                DebugAppStyle.backgroundColourEdit = backgroundColour.unserialize

                DebugAppStyle.readOnlyBackgroundColourEdit = readOnlyBackgroundColour.unserialize
                DebugAppStyle.readOnlyBorderColourEdit = readOnlyBorderColour.unserialize
                DebugAppStyle.readOnlyClearIconColourEdit = readOnlyClearIconColour.unserialize

                debugPrimaryButtonColours.run {
                    DebugAppStyle.primaryButtonBorderColour = primaryButtonBorderColour.unserialize
                    DebugAppStyle.primaryButtonContentColour = primaryButtonContentColour.unserialize
                    DebugAppStyle.primaryButtonBackgroundColour = primaryButtonBackgroundColour.unserialize
                    DebugAppStyle.primaryButtonDisabledBorderColour = primaryButtonDisabledBorderColour.unserialize
                    DebugAppStyle.primaryButtonDisabledContentColour = primaryButtonDisabledContentColour.unserialize
                    DebugAppStyle.primaryButtonDisabledBackgroundColour =
                        primaryButtonDisabledBackgroundColour.unserialize
                }

                debugSecondaryButtonColours.run {
                    DebugAppStyle.secondaryButtonBorderColour = secondaryButtonBorderColour.unserialize
                    DebugAppStyle.secondaryButtonContentColour = secondaryButtonContentColour.unserialize
                    DebugAppStyle.secondaryButtonBackgroundColour = secondaryButtonBackgroundColour.unserialize
                    DebugAppStyle.secondaryButtonDisabledBorderColour = secondaryButtonDisabledBorderColour.unserialize
                    DebugAppStyle.secondaryButtonDisabledContentColour =
                        secondaryButtonDisabledContentColour.unserialize
                    DebugAppStyle.secondaryButtonDisabledBackgroundColour =
                        secondaryButtonDisabledBackgroundColour.unserialize
                }

                debugTextInputColours.run {
                    DebugAppStyle.textInputBorder = border.unserialize
                    DebugAppStyle.textInputBackground = background.unserialize
                    DebugAppStyle.textInputContent = content.unserialize
                    DebugAppStyle.textInputHint = hint.unserialize
                    DebugAppStyle.textInputLabel = label.unserialize
                }

                debugTextColours.run {
                    DebugAppStyle.headerTextColour = headerTextColour.unserialize
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

    fun updateServerAccessCode(accessCode: String) {
        launch {
            dataStore.updateData {
                it.copy(serverAccessCode = accessCode).also {
                    AndroidServer.server.value?.updateAccessCode(accessCode)
                }
            }
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
            val oldColours = it.debugSettings.colourSettings
            val resetSettings = SettingsDataAndroid()
            resetSettings.copy(debugSettings = resetSettings.debugSettings.copy(colourSettings = oldColours))
        }
    }

    fun resetColours() = launch {
        dataStore.updateData {
            it.copy(debugSettings = it.debugSettings.copy(colourSettings = SettingsDataAndroid.DebugAppStyleColours()))
        }
    }

    fun saveColours() = launch {
        dataStore.updateData { data ->
            data.copy(
                debugSettings = data.debugSettings.copy(
                    colourSettings = SettingsDataAndroid.DebugAppStyleColours(
                        statusBarColour = DebugAppStyle.statusBarColourEdit.serialize,
                        headerBackgroundColour = DebugAppStyle.headerBackgroundColourEdit.serialize,
                        footerBackgroundColour = DebugAppStyle.footerBackgroundColourEdit.serialize,
                        backgroundColour = DebugAppStyle.backgroundColourEdit.serialize,
                        readOnlyBorderColour = DebugAppStyle.readOnlyBorderColourEdit.serialize,
                        readOnlyBackgroundColour = DebugAppStyle.readOnlyBackgroundColourEdit.serialize,
                        readOnlyClearIconColour = DebugAppStyle.readOnlyClearIconColourEdit.serialize,
                        debugPrimaryButtonColours = SettingsDataAndroid.DebugPrimaryButtonColours(
                            primaryButtonBorderColour = DebugAppStyle.primaryButtonBorderColour.serialize,
                            primaryButtonContentColour = DebugAppStyle.primaryButtonContentColour.serialize,
                            primaryButtonBackgroundColour = DebugAppStyle.primaryButtonBackgroundColour.serialize,
                            primaryButtonDisabledBorderColour = DebugAppStyle
                                .primaryButtonDisabledBorderColour
                                .serialize,
                            primaryButtonDisabledContentColour = DebugAppStyle
                                .primaryButtonDisabledContentColour
                                .serialize,
                            primaryButtonDisabledBackgroundColour = DebugAppStyle
                                .primaryButtonDisabledBackgroundColour
                                .serialize,
                        ),
                        debugSecondaryButtonColours = SettingsDataAndroid.DebugSecondaryButtonColours(
                            secondaryButtonBorderColour = DebugAppStyle.secondaryButtonBorderColour.serialize,
                            secondaryButtonContentColour = DebugAppStyle.secondaryButtonContentColour.serialize,
                            secondaryButtonBackgroundColour = DebugAppStyle.secondaryButtonBackgroundColour.serialize,
                            secondaryButtonDisabledBorderColour = DebugAppStyle
                                .secondaryButtonDisabledBorderColour
                                .serialize,
                            secondaryButtonDisabledContentColour = DebugAppStyle
                                .secondaryButtonDisabledContentColour
                                .serialize,
                            secondaryButtonDisabledBackgroundColour = DebugAppStyle
                                .secondaryButtonDisabledBackgroundColour
                                .serialize,
                        ),
                        debugTextInputColours = SettingsDataAndroid.DebugTextInputColours(
                            border = DebugAppStyle.textInputBorder.serialize,
                            background = DebugAppStyle.textInputBackground.serialize,
                            content = DebugAppStyle.textInputContent.serialize,
                            hint = DebugAppStyle.textInputHint.serialize,
                            label = DebugAppStyle.textInputLabel.serialize,
                        ),
                        debugTextColours = SettingsDataAndroid.DebugTextColours(
                            headerTextColour = DebugAppStyle.headerTextColour.serialize,
                        ),
                    ),
                ),
            )
        }
    }
}
