package org.hmeadow.fittonia.screens.debugScreen

import SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.PuPrKeyCipher.ENCRYPT_MAX_BYTES_ALLOWED
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.compose.components.InputFlowCollectionLauncher
import org.hmeadow.fittonia.hmeadowSocket.AESCipher
import org.hmeadow.fittonia.mainActivity.MainActivity
import org.hmeadow.fittonia.mainActivity.MainViewModel
import org.hmeadow.fittonia.utility.createJobDirectory
import kotlin.math.abs
import kotlin.random.Random

internal class DebugScreenViewModel(
    private val mainViewModel: MainViewModel,
) : BaseViewModel() {
    fun initDebugInputFlow(initial: String, onValueChange: ((String) -> Unit)? = null): InputFlow {
        val inputFlowCollectionLauncher = InputFlowCollectionLauncher()
        return InputFlow(initial = initial, onValueChange, inputFlowCollectionLauncher).also {
            inputFlowCollectionLauncher.launch()
        }
    }

    val deviceIp = MutableStateFlow("Unknown")

    // Defaults
    val defaultSendThrottle = initInputFlow(initial = "")
    val defaultNewDestinationName = initInputFlow(initial = "")
    val defaultNewDestinationPort = initInputFlow(initial = "")
    val defaultNewDestinationAccessCode = initInputFlow(initial = "")
    val defaultNewDestinationIP = initInputFlow(initial = "")
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
    val maxEncryptionBytesPuPr = ENCRYPT_MAX_BYTES_ALLOWED
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
                defaultNewDestinationAccessCode.text = debugSettings.defaultNewDestinationAccessCode
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
            when (MainActivity.mainActivity.createJobDirectory(jobName = null)) {
                is MainActivity.CreateDumpDirectory.Success -> {
                    nextAutoJobNameMessage.update {
                        nextAutoJobName.first().let { nextAutoJobName ->
                            if (nextAutoJobName != expectedJobNumber + 1) {
                                "Success! Job$expectedJobNumber already existed so created " +
                                    "Job${nextAutoJobName - 1} instead."
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
                accessCode = defaultNewDestinationAccessCode.text,
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
                    PuPrKeyCipher.encrypt(sb.toString().encodeToByteArray(), publicKey)
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
