package org.hmeadow.fittonia.screens

import SettingsManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.utility.Debug

class NewDestinationScreenViewModel(
    oneTimeIp: String?,
    oneTimeAccessCode: String?,
    private val onSaveNewDestinationCallback: (SettingsManager.Destination) -> Unit,
) : BaseViewModel() {
    val nameState = InputFlow(initial = "")
    val ipAddressState = InputFlow(initial = oneTimeIp ?: "")
    val accessCodeState = InputFlow(initial = oneTimeAccessCode ?: "")

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
}

@Composable
fun NewDestinationScreen(
    viewModel: NewDestinationScreenViewModel,
    onBackClicked: () -> Unit,
) {
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = "New destination",
                onBackClicked = onBackClicked,
            )
        },
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FittoniaSpacerHeight(height = 15)

                Text(
                    text = stringResource(R.string.new_destination_screen_body),
                    style = paragraphTextStyle,
                )

                FittoniaSpacerHeight(height = 7)

                Text(
                    text = "The destination will be pinged to confirm its existence.",
                    style = paragraphTextStyle,
                )

                FittoniaSpacerHeight(height = 30)

                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.nameState,
                    label = "Name",
                )

                FittoniaSpacerHeight(height = 30)

                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.ipAddressState,
                    label = "IP Address/Code",
                )

                Debug {
                    FittoniaButton(
                        onClick = {
                            viewModel.ipAddressState.text = MainActivity.mainActivity.getDeviceIpAddress() ?: ""
                        },
                    ) {
                        ButtonText("<Debug fill with this device's IP>")
                    }
                }

                FittoniaSpacerHeight(height = 30)

                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.accessCodeState,
                    label = "Access Code",
                )
            }
        },
        footer = {
            Footer {
                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::onSaveNewDestination,
                    enabled = viewModel.canAddDestination.collectAsState(initial = false).value,
                    content = {
                        ButtonText(text = "Save")
                        FittoniaSpacerWidth(width = 5)
                        ButtonIcon(drawableRes = R.drawable.ic_save)
                    },
                )
            }
        },
    )
}

@Composable
@Preview
private fun Preview() {
    NewDestinationScreen(
        viewModel = NewDestinationScreenViewModel(
            oneTimeIp = null,
            oneTimeAccessCode = null,
            onSaveNewDestinationCallback = {},
        ),
        onBackClicked = { },
    )
}
