package org.hmeadow.fittonia.screens.newDestinationScreen

import SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.components.EquivalentIPCode
import org.hmeadow.fittonia.components.decipherIpAndCode
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.utility.ContinueFlow
import org.hmeadow.fittonia.utility.DestinationPing
import org.hmeadow.fittonia.utility.canContinue
import org.hmeadow.fittonia.utility.debug

internal class NewDestinationScreenViewModel(
    oneTimeIp: String?,
    oneTimeAccessCode: String?,
    private val onSaveNewDestinationCallback: (SettingsManager.Destination) -> Unit,
    private val onPing: suspend (ip: String, port: Int, accessCode: String, requestTimestamp: Long) -> Ping,
) : BaseViewModel(), DestinationPing {
    val portState: String = debug(debugValue = "44556", releaseValue = "44556")
    val nameState: InputFlow = initInputFlow(
        initial = "",
        onValueChange = { _ ->
            updatePing(
                ip = ipAddressState.text,
                accessCode = accessCodeState.text,
                port = portState.toInt(),
                onPing = onPing,
            )
        },
    )
    val nameContinue: ContinueFlow<String> = ContinueFlow(flow = nameState) { name ->
        if (name.isNotEmpty()) {
            ContinueFlow.ContinueFlag.Pass
        } else {
            ContinueFlow.ContinueFlag.Fail
        }
    }
    val equivelentIpOrCode: MutableStateFlow<EquivalentIPCode> = MutableStateFlow(value = EquivalentIPCode.Neither)
    val ipAddressState: InputFlow = initInputFlow(initial = oneTimeIp ?: "") { ip ->
        equivelentIpOrCode.update { decipherIpAndCode(ip = ip) }
        updatePing(
            ip = ip,
            accessCode = accessCodeState.text,
            port = portState.toInt(),
            onPing = onPing,
        )
    }
    val ipAddressContinue: ContinueFlow<String> = ContinueFlow(flow = ipAddressState) { ipAddress ->
        if (ipAddress.isNotEmpty() && decipherIpAndCode(ipAddress) !is EquivalentIPCode.Neither) {
            ContinueFlow.ContinueFlag.Pass
        } else {
            ContinueFlow.ContinueFlag.Fail
        }
    }
    val accessCodeState: InputFlow = initInputFlow(
        initial = oneTimeAccessCode ?: "",
        onValueChange = { accessCode ->
            updatePing(
                ip = ipAddressState.text,
                accessCode = accessCode,
                port = portState.toInt(),
                onPing = onPing,
            )
        },
    )
    val accessCodeContinue: ContinueFlow<String> = ContinueFlow(flow = accessCodeState) { accessCode ->
        if (accessCode.isNotEmpty()) {
            ContinueFlow.ContinueFlag.Pass
        } else {
            ContinueFlow.ContinueFlag.Fail
        }
    }
    override val ping = MutableStateFlow(value = Ping(PingStatus.NoPing))

    val canAddDestination = combine(
        nameContinue.result,
        ipAddressContinue.result,
        accessCodeContinue.result,
    ) { name, ip, accessCode ->
        name.canContinue && ip.canContinue && accessCode.canContinue
    }

    fun onSaveNewDestination() {
        onSaveNewDestinationCallback(
            SettingsManager.Destination(
                name = nameState.text,
                ip = ipAddressState.text,
                accessCode = accessCodeState.text,
            ),
        )
    }

    init {
        launchInputFlows()
    }
}
