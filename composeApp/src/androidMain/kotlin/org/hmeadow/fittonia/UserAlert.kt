package org.hmeadow.fittonia

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

sealed class UserAlert {
    sealed interface Error
    sealed interface Warning
    sealed interface Info

    data class PortInUse(val port: Int) : UserAlert(), Error

    companion object {
        val userAlerts: MutableStateFlow<List<UserAlert>> = MutableStateFlow(emptyList())
        val hasAlerts = userAlerts.map { it.isNotEmpty() }
    }
}

@Composable
fun UserAlert.title(): String = when (this) {
    is UserAlert.PortInUse -> stringResource(id = R.string.notification_default_port_in_use_title)
}

@Composable
fun UserAlert.description(): String = when (this) {
    is UserAlert.PortInUse -> stringResource(id = R.string.notification_default_port_in_use_description, port)
}
