package org.hmeadow.fittonia.screens

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.MainViewModel
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.ReadOnlyEntries
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaNumberInput
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.inputLabelStyle
import org.hmeadow.fittonia.design.fonts.paragraphParagraphStyle
import org.hmeadow.fittonia.design.fonts.paragraphSpanStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.design.fonts.psstStyle
import org.hmeadow.fittonia.utility.Debug
import org.hmeadow.fittonia.utility.InfoBorderState.infoBoxOverlay
import psstColour

class WelcomeScreenViewModel(
    private val mainViewModel: MainViewModel,
    private val onContinueCallback: (accessCode: String, port: Int) -> Unit,
) : BaseViewModel() {
    val serverAccessCodeState = InputFlow(initial = "")
    val portFieldState = InputFlow(initial = "")

    val canContinue = combine(
        serverAccessCodeState,
        mainViewModel.dataStore.data,
    ) { accessCodeState, dumpPathState ->
        accessCodeState.isNotEmpty() && dumpPathState.dumpPath.dumpUriPath.isNotEmpty()
    }

    fun onContinue() {
        onContinueCallback(
            serverAccessCodeState.text,
            portFieldState.text.toIntOrNull() ?: 44556, // TODO - after release somewhere to put constants.
        )
    }

    fun onDumpPathPicked(path: Uri) {
        mainViewModel.updateDumpPath(path)
    }
}

@Composable
fun WelcomeScreen(
    viewModel: WelcomeScreenViewModel,
    data: SettingsDataAndroid,
    onClearDumpPath: () -> Unit,
) {
    FittoniaScaffold(
        header = { FittoniaHeader() },
        content = {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Text(
                    text = stringResource(id = R.string.welcome_screen_title),
                    style = headingLStyle,
                )

                Debug {
                    FittoniaButton(
                        onClick = {
                            viewModel.portFieldState.text = "12345"
                            viewModel.serverAccessCodeState.text = "accesscode"
                        },
                    ) {
                        ButtonText(text = "<Debug Fill Values>")
                    }
                }

                FittoniaSpacerHeight(height = 40)

                Text(
                    text = stringResource(R.string.welcome_screen_body),
                    style = paragraphTextStyle,
                )

                FittoniaSpacerHeight(height = 3)

                Text(
                    text = stringResource(id = R.string.welcome_screen_you_can_change_these_later),
                    style = psstStyle,
                    color = psstColour,
                )

                FittoniaSpacerHeight(height = 15)

                AccessCodeField(
                    modifier = Modifier.fillMaxWidth(),
                    serverAccessCodeState = viewModel.serverAccessCodeState,
                )

                FittoniaSpacerHeight(height = 20)

                FittoniaNumberInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.portFieldState,
                    hint = "Default: 44556",
                    label = {
                        Text(
                            text = stringResource(id = R.string.welcome_screen_new_server_port_label),
                            style = inputLabelStyle,
                        )
                    },
                )

                FittoniaSpacerHeight(height = 20)

                Text(
                    text = stringResource(id = R.string.welcome_screen_new_destination_folder_label),
                    style = inputLabelStyle,
                )
                ReadOnlyEntries(
                    entries = listOf(data.dumpPath.dumpPathReadable),
                    onEntryClearClicked = { onClearDumpPath() },
                    expandOnClick = true,
                )

                FittoniaSpacerHeight(height = 7)

                FittoniaButton(
                    onClick = { MainActivity.mainActivity.openFolderPicker(viewModel::onDumpPathPicked) },
                    onInfo = {
                        Column {
                            Text(
                                text = stringResource(R.string.welcome_screen_select_folder_button_info_1),
                                style = paragraphTextStyle,
                            )
                            FittoniaSpacerHeight(height = 10)
                            Text(
                                text = stringResource(R.string.welcome_screen_select_folder_button_info_2),
                                style = paragraphTextStyle,
                            )
                            FittoniaSpacerHeight(height = 10)
                            Text(
                                text = stringResource(R.string.welcome_screen_select_folder_button_info_3),
                                style = paragraphTextStyle,
                            )
                        }
                    },
                    content = {
                        ButtonText(
                            text = stringResource(id = R.string.welcome_screen_new_destination_select_folder_button),
                        )
                    },
                )
            }
        },
        footer = {
            Footer {
                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewModel.canContinue.collectAsState(initial = false).value,
                    onClick = viewModel::onContinue,
                    content = { ButtonText(text = stringResource(id = R.string.welcome_screen_continue_button)) },
                )
            }
        },
        overlay = {
            infoBoxOverlay()
        },
    )
}

@Composable
private fun AccessCodeField(serverAccessCodeState: InputFlow, modifier: Modifier = Modifier) {
    val info1 = stringResource(R.string.welcome_screen_access_code_field_info_1)
    val info2 = stringResource(R.string.welcome_screen_access_code_field_info_2)
    val info3 = stringResource(R.string.welcome_screen_access_code_field_info_3)
    val paraSpanStyle = paragraphSpanStyle
    FittoniaTextInput(
        modifier = modifier,
        inputFlow = serverAccessCodeState,
        onInfo = {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = paragraphParagraphStyle) {
                        withStyle(style = paraSpanStyle) {
                            append(text = info1)
                            append(text = " ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(text = info2) }
                            append(text = " ")
                            append(text = info3)
                        }
                    }
                },
                style = paragraphTextStyle,
            )
            FittoniaSpacerHeight(height = 10)
            Text(
                text = stringResource(R.string.welcome_screen_access_code_field_info_4),
                style = paragraphTextStyle,
            )
        },
        label = {
            Text(
                text = stringResource(id = R.string.welcome_screen_new_server_access_code_label),
                style = inputLabelStyle,
            )
        },
    )
}
