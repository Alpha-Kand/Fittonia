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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.EquivalentIPCode
import org.hmeadow.fittonia.components.EquivalentIpCodeText
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.decipherIpAndCode
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.utility.Debug

class NewDestinationScreenViewModel(
    oneTimeIp: String?,
    oneTimeAccessCode: String?,
    private val onSaveNewDestinationCallback: (SettingsManager.Destination) -> Unit,
) : BaseViewModel() {
    val nameState = InputFlow(initial = "")
    val equivelentIpOrCode: MutableStateFlow<EquivalentIPCode> = MutableStateFlow(value = EquivalentIPCode.Neither)
    val ipAddressState = InputFlow(initial = oneTimeIp ?: "") { ip ->
        equivelentIpOrCode.update { decipherIpAndCode(ip = ip) }
    }
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
            Column(modifier = Modifier.padding(horizontal = spacing16)) {
                FittoniaSpacerHeight(height = spacing16)

                Text(
                    text = stringResource(R.string.new_destination_screen_body),
                    style = paragraphTextStyle,
                )

                FittoniaSpacerHeight(height = spacing8)

                Text(
                    text = "The destination will be pinged to confirm its existence.",
                    style = paragraphTextStyle,
                )

                FittoniaSpacerHeight(height = spacing32)

                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.nameState,
                    label = "Name",
                )

                FittoniaSpacerHeight(height = spacing32)

                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.ipAddressState,
                    label = "IP Address/Code",
                )

                FittoniaSpacerHeight(height = spacing4)

                EquivalentIpCodeText(equivalentIPCode = viewModel.equivelentIpOrCode.collectAsState().value)

                Debug {
                    FittoniaButton(
                        onClick = {
                            viewModel.ipAddressState.text = MainActivity.mainActivity.getDeviceIpAddress() ?: ""
                        },
                    ) {
                        ButtonText("<Debug fill with this device's IP>")
                    }
                }

                FittoniaSpacerHeight(height = spacing32)

                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.accessCodeState,
                    label = "Access Code",
                )

                FittoniaSpacerHeight(height = spacing32)
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
                        FittoniaSpacerWidth(width = spacing4)
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
