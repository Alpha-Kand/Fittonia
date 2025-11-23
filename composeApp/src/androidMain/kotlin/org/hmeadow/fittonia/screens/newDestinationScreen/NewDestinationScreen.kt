package org.hmeadow.fittonia.screens.newDestinationScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.EquivalentIPCode
import org.hmeadow.fittonia.components.EquivalentIpCodeText
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.PingStatusComponent
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.utility.ContinueStatusIcon
import org.hmeadow.fittonia.utility.pingStatus

@Composable
internal fun NewDestinationScreen(
    viewModel: NewDestinationScreenViewModel,
    onBackClicked: () -> Unit,
) {
    FittoniaScaffold(
        header = {
            FittoniaHeader(
                headerText = stringResource(R.string.new_destination_heading),
                onBackClicked = onBackClicked,
            )
        },
        content = {
            Column(modifier = Modifier.padding(horizontal = spacing16)) {
                FittoniaSpacerHeight(height = spacing16)

                Text(
                    text = stringResource(R.string.new_destination_screen_body_1),
                    style = paragraphTextStyle,
                )

                FittoniaSpacerHeight(height = spacing8)

                Text(
                    text = stringResource(R.string.new_destination_screen_body_2),
                    style = paragraphTextStyle,
                )

                FittoniaSpacerHeight(height = spacing32)

                Row(verticalAlignment = Alignment.Bottom) {
                    ContinueStatusIcon(continueStatus = viewModel.nameContinue.collect())

                    FittoniaSpacerWidth(width = spacing16)

                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.nameState,
                        label = stringResource(R.string.new_destination_name_label),
                    )
                }

                FittoniaSpacerHeight(height = spacing32)

                Row(verticalAlignment = Alignment.Bottom) {
                    val equivelentIpOrCode = viewModel.equivelentIpOrCode.collectAsState().value
                    val continueStatusBottomPadding = if (equivelentIpOrCode !is EquivalentIPCode.Neither) {
                        23.dp
                    } else {
                        0.dp
                    }

                    ContinueStatusIcon(
                        modifier = Modifier.padding(bottom = continueStatusBottomPadding),
                        continueStatus = viewModel.ipAddressContinue.collect(),
                    )

                    FittoniaSpacerWidth(width = spacing16)

                    Column {
                        FittoniaTextInput(
                            modifier = Modifier.fillMaxWidth(),
                            inputFlow = viewModel.ipAddressState,
                            label = stringResource(R.string.new_destination_ip_address_label),
                            onFocusChanged = viewModel.ipAddressContinue::focusChanged,
                        )

                        FittoniaSpacerHeight(height = spacing4)

                        EquivalentIpCodeText(equivalentIPCode = viewModel.equivelentIpOrCode.collectAsState().value)
                    }
                }

                FittoniaSpacerHeight(height = spacing32)

                Row(verticalAlignment = Alignment.Bottom) {
                    ContinueStatusIcon(continueStatus = viewModel.accessCodeContinue.collect())

                    FittoniaSpacerWidth(width = spacing16)

                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.accessCodeState,
                        label = stringResource(R.string.new_destination_access_code_label),
                    )
                }

                FittoniaSpacerHeight(height = spacing32)

                PingStatusComponent(pingStatus = viewModel.pingStatus)

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
                        ButtonText(text = stringResource(R.string.new_destination_save_button))
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
            onPing = { _, _, _, _ -> Ping(PingStatus.NoPing, 0) },
        ),
        onBackClicked = { },
    )
}
