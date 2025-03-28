package org.hmeadow.fittonia.screens.debugScreen

import SettingsManager
import org.hmeadow.fittonia.hmeadowSocket.AESCipher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.MainViewModel
import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.components.InputFlow
import kotlin.math.abs
import kotlin.random.Random

class DebugScreenViewModel(
    private val mainViewModel: MainViewModel,
) : BaseViewModel() {
    val deviceIp = MutableStateFlow("Unknown")

    // Defaults
    val defaultSendThrottle = InputFlow(initial = "")
    val defaultNewDestinationName = InputFlow(initial = "")
    val defaultNewDestinationPort = InputFlow(initial = "")
    val defaultNewDestinationPassword = InputFlow(initial = "")
    val defaultNewDestinationIP = InputFlow(initial = "")
    val needToSave = combine(
        mainViewModel.dataStore.data,
        defaultNewDestinationPort,
    ) { data, newDefaultDestinationPort ->
        newDefaultDestinationPort != data.debugSettings.defaultNewDestinationPort.toString()
    }

    // Admin Create
    val nextAutoJobName = mainViewModel.dataStore.data.map { it.nextAutoJobName }
    val nextAutoJobNameMessage = MutableStateFlow("")

    // Public/Private Key Encryption Test
    val maxEncryptionBytesPuPr = 245
    val encryptedMessagePuPr = MutableStateFlow(ByteArray(0))
    val decryptedMessagePuPr = MutableStateFlow("")

    // AES Encryption Test
    private val keyAES = AESCipher.generateKey()
    val maxEncryptionBytesAES = 8192
    val encryptedMessageAES = MutableStateFlow(ByteArray(0))
    val decryptedMessageAES = MutableStateFlow("")

    init {
        launch {
            mainViewModel.dataStore.data.first().run {
                defaultSendThrottle.text = debugSettings.defaultSendThrottle.toString()
                defaultNewDestinationName.text = debugSettings.defaultNewDestinationName
                defaultNewDestinationPort.text = debugSettings.defaultNewDestinationPort.toString()
                defaultNewDestinationIP.text = debugSettings.defaultNewDestinationIP
                defaultNewDestinationPassword.text = debugSettings.defaultNewDestinationPassword
            }
        }
        refreshIp()
    }

    fun refreshIp() {
        deviceIp.value = MainActivity.mainActivity.getDeviceIpAddress() ?: "Unknown"
    }

    fun createJobDirectory() {
        launch {
            val expectedJobNumber = nextAutoJobName.first()
            when (MainActivity.mainActivity.createJobDirectory(jobName = null, print = ::println)) {
                is MainActivity.CreateDumpDirectory.Success -> {
                    nextAutoJobNameMessage.update {
                        nextAutoJobName.first().let { nextAutoJobName ->
                            if (nextAutoJobName != expectedJobNumber + 1) {
                                "Success! Job$expectedJobNumber already existed so created Job${nextAutoJobName - 1} instead."
                            } else {
                                "Success! Created Job${nextAutoJobName - 1}"
                            }
                        }
                    }
                }

                else -> nextAutoJobNameMessage.update { "Error!" }
            }
        }
    }

    fun createNewDestination() {
        val getIpNum = { abs(Random.nextInt() % 256) }
        mainViewModel.addDestination(
            destination = SettingsManager.Destination(
                name = "$defaultNewDestinationName ${abs(Random.nextInt() % 100)}",
                ip = "${getIpNum()}.${getIpNum()}.${getIpNum()}.${getIpNum()}",
                password = defaultNewDestinationPassword.text,
            ),
        )
    }

    fun onSaveDefaults() {
        launch {
            mainViewModel.dataStore.updateData {
                it.copy(
                    debugSettings = it.debugSettings.copy(
                        defaultNewDestinationPort = defaultNewDestinationPort.text.toIntOrNull()
                            ?: it.debugSettings.defaultNewDestinationPort,
                    ),
                )
            }
        }
    }

    fun onEncryptMessagePuPr() {
        PuPrKeyCipher.getPublicKeyFromKeyStore()?.let { publicKey ->
            launch {
                encryptedMessagePuPr.update {
                    val sb = StringBuilder()
                    repeat(maxEncryptionBytesPuPr) {
                        sb.append('a')
                    }
                    PuPrKeyCipher.encrypt(sb.toString().encodeToByteArray(), publicKey.encoded)
                }
            }
        }
    }

    fun onDecryptMessagePuPr() {
        launch {
            decryptedMessagePuPr.update {
                PuPrKeyCipher.decrypt(encryptedMessagePuPr.value).decodeToString()
            }
        }
    }

    fun onEncryptMessageAES() {
        encryptedMessageAES.update {
            val sb = StringBuilder()
            repeat(maxEncryptionBytesAES) {
                sb.append('a')
            }
            AESCipher.encryptBytes(bytes = sb.toString().encodeToByteArray(), secretKeyBytes = keyAES)
        }
    }

    fun onDecryptMessageAES() {
        launch {
            decryptedMessageAES.update {
                AESCipher.decryptBytes(bytes = encryptedMessageAES.value, secretKeyBytes = keyAES).decodeToString()
            }
        }
    }
}
