package org.hmeadow.fittonia.screens.debugScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaNumberInput
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.InputFlow
import org.hmeadow.fittonia.components.socketPortFilters
import org.hmeadow.fittonia.design.fonts.headingLStyle

@Composable
fun DebugScreenDefaultsTab(
    defaultNewDestinationName: InputFlow,
    defaultNewDestinationPort: InputFlow,
    defaultNewDestinationPassword: InputFlow,
    defaultNewDestinationIP: InputFlow,
    onSaveDefaults: () -> Unit,
    needToSave: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = "Defaults",
            style = headingLStyle,
        )
        HMSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default New Destination Name",
            inputFlow = defaultNewDestinationName,
        )
        HMSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default New Destination Port",
            filters = socketPortFilters,
            inputFlow = defaultNewDestinationPort,
        )
        HMSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default New Destination Password",
            inputFlow = defaultNewDestinationPassword,
        )
        HMSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default New Destination IP Address",
            inputFlow = defaultNewDestinationIP,
        )
        HMSpacerHeight(height = 10)
        FittoniaButton(
            onClick = onSaveDefaults,
            enabled = needToSave,
        ) {
            ButtonText(text = "Save")
        }
    }
}
