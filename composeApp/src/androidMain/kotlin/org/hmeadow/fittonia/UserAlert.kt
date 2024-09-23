package org.hmeadow.fittonia

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

sealed class UserAlert {
    val id: Int
        get() = hashCode()
    abstract val numAllowed: Int

    sealed interface Error
    sealed interface Warning
    sealed interface Info

    data class PortInUse(val port: Int) : UserAlert(), Error {
        override val numAllowed: Int
            get() = 1
    }

    object DumpLocationLost : UserAlert(), Error {
        override val numAllowed: Int
            get() = 1
    }

    companion object {
        val userAlerts: MutableStateFlow<List<UserAlert>> = MutableStateFlow(emptyList())
        val hasAlerts = userAlerts.map { it.isNotEmpty() }
    }
}

@Composable
fun UserAlert.title(): String = when (this) {
    is UserAlert.PortInUse -> stringResource(id = R.string.notification_default_port_in_use_title)
    is UserAlert.DumpLocationLost -> "'Incoming files' destination permissions lost."
}

@Composable
fun UserAlert.description(): String = when (this) {
    is UserAlert.PortInUse -> stringResource(id = R.string.notification_default_port_in_use_description, port)
    is UserAlert.DumpLocationLost -> "The permission to access your previously selected 'Incoming files' destination has been lost or the destination has been moved/deleted. Please select a new destination."
}
