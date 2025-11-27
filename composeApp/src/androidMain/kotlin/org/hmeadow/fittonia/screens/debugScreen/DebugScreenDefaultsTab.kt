package org.hmeadow.fittonia.screens.debugScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaNumberInput
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.compose.components.socketPortFilters
import org.hmeadow.fittonia.design.fonts.headingLStyle

@Composable
fun DebugScreenDefaultsTab(
    defaultSendThrottle: InputFlow,
    defaultNewDestinationName: InputFlow,
    defaultNewDestinationPort: InputFlow,
    defaultNewDestinationAccessCode: InputFlow,
    defaultNewDestinationIP: InputFlow,
    onSaveDefaults: () -> Unit,
    needToSave: Boolean,
    footerHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Defaults",
            style = headingLStyle,
        )
        FittoniaSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default Send Throttle mb",
            inputFlow = defaultSendThrottle,
        )
        FittoniaSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default New Destination Name",
            inputFlow = defaultNewDestinationName,
        )
        FittoniaSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default New Destination Port",
            filters = socketPortFilters,
            inputFlow = defaultNewDestinationPort,
        )
        FittoniaSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default New Destination Access Code",
            inputFlow = defaultNewDestinationAccessCode,
        )
        FittoniaSpacerHeight(height = 10)
        FittoniaNumberInput(
            label = "Default New Destination IP Address",
            inputFlow = defaultNewDestinationIP,
        )
        FittoniaSpacerHeight(height = 10)
        FittoniaButton(
            onClick = onSaveDefaults,
            enabled = needToSave,
        ) {
            ButtonText(text = "Save")
        }
        FittoniaSpacerHeight(footerHeight)
    }
}
