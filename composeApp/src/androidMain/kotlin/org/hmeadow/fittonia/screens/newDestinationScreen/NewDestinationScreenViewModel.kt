package org.hmeadow.fittonia.screens.newDestinationScreen

import SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.components.EquivalentIPCode
import org.hmeadow.fittonia.components.decipherIpAndCode

internal class NewDestinationScreenViewModel(
    oneTimeIp: String?,
    oneTimeAccessCode: String?,
    private val onSaveNewDestinationCallback: (SettingsManager.Destination) -> Unit,
) : BaseViewModel() {
    val nameState = initInputFlow(initial = "")
    val equivelentIpOrCode: MutableStateFlow<EquivalentIPCode> = MutableStateFlow(value = EquivalentIPCode.Neither)
    val ipAddressState = initInputFlow(initial = oneTimeIp ?: "") { ip ->
        equivelentIpOrCode.update { decipherIpAndCode(ip = ip) }
    }
    val accessCodeState = initInputFlow(initial = oneTimeAccessCode ?: "")

    val canAddDestination = combine(
        nameState,
        ipAddressState,
        accessCodeState,
    ) { name, ip, accessCode ->
        name.isNotEmpty() && ip.isNotEmpty() && accessCode.isNotEmpty()
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
