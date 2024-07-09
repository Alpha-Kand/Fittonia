package org.hmeadow.fittonia.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.BuildConfig
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.MainViewModel
import org.hmeadow.fittonia.Navigator
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.FittoniaBackground
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaNumberInput
import org.hmeadow.fittonia.components.FittoniaTextInput
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.InputFlow
import org.hmeadow.fittonia.components.ReadOnlyEntries
import org.hmeadow.fittonia.components.headingLStyle
import org.hmeadow.fittonia.components.inputLabelStyle
import org.hmeadow.fittonia.components.paragraphStyle
import org.hmeadow.fittonia.components.psstColour
import org.hmeadow.fittonia.components.psstStyle

class WelcomeScreenViewModel(
    mainViewModel: MainViewModel,
    private val onContinueCallback: (password: String, port: Int) -> Unit,
) : BaseViewModel {
    val serverPasswordState = InputFlow(initial = "")
    val portFieldState = InputFlow(initial = "")

    val canContinue = combine(
        serverPasswordState,
        portFieldState,
        mainViewModel.dataStore.data,
    ) { passwordState, portState, dumpPathState ->
        passwordState.isNotEmpty() && portState.isNotEmpty() && dumpPathState.dumpPath.isNotEmpty()
    }

    fun onContinue() {
        onContinueCallback(
            serverPasswordState.value,
            portFieldState.value.toInt(),
        )
    }
}

@Composable
fun WelcomeScreen(
    viewModel: WelcomeScreenViewModel,
    data: SettingsDataAndroid,
    onClearDumpPath: () -> Unit,
) {
    FittoniaBackground(
        header = {
            Row {
                if (BuildConfig.DEBUG) {
                    HMSpacerWeightRow()
                    FittoniaIcon(
                        modifier = Modifier.clickable(onClick = Navigator::goToDebugScreen),
                        drawableRes = R.drawable.ic_debug,
                        tint = Color.Cyan,
                    )
                }
            }
        },
        content = {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Text(
                    text = stringResource(id = R.string.welcome_screen_title),
                    style = headingLStyle,
                )

                HMSpacerHeight(height = 40)

                Text(
                    text = stringResource(R.string.welcome_screen_body),
                    style = paragraphStyle,
                )
                HMSpacerHeight(height = 3)
                Text(
                    text = stringResource(id = R.string.welcome_screen_you_can_change_these_later),
                    style = psstStyle,
                    color = psstColour,
                )
                HMSpacerHeight(height = 15)
                Row {
                    Text(
                        text = stringResource(id = R.string.welcome_screen_new_server_password_label),
                        style = inputLabelStyle,
                    )
                    // TODO: Help Icon
                }
                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.serverPasswordState,
                )

                HMSpacerHeight(height = 15)

                Row {
                    Text(
                        text = stringResource(id = R.string.welcome_screen_new_server_port_label),
                        style = inputLabelStyle,
                    )
                    // TODO: Help Icon
                }
                FittoniaNumberInput(
                    modifier = Modifier.fillMaxWidth(),
                    inputFlow = viewModel.portFieldState,
                )

                HMSpacerHeight(height = 15)

                Row {
                    Text(
                        text = stringResource(id = R.string.welcome_screen_new_destination_folder_label),
                        style = inputLabelStyle,
                    )
                    // TODO: Help Icon
                }
                ReadOnlyEntries(
                    entries = listOf(data.dumpPath),
                    onEntryClearClicked = { onClearDumpPath() },
                    expandOnClick = true,
                )
                FittoniaButton(
                    onClick = {
                        MainActivity.mainActivity.openDumpPicker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
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
    )
}
