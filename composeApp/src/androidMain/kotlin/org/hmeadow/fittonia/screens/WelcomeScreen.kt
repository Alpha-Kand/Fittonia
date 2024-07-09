package org.hmeadow.fittonia.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.BuildConfig
import org.hmeadow.fittonia.MainActivity
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
import org.hmeadow.fittonia.components.headingLStyle
import org.hmeadow.fittonia.components.inputLabelStyle
import org.hmeadow.fittonia.components.paragraphStyle
import org.hmeadow.fittonia.components.psstColour
import org.hmeadow.fittonia.components.psstStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    data: SettingsDataAndroid,
    onContinue: (password: String, port: Int) -> Unit,
) {
    val serverPasswordState = rememberTextFieldState(initialText = "")
    val portFieldState = rememberTextFieldState(initialText = "")
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
                    text = "Welcome!",
                    style = headingLStyle,
                )

                HMSpacerHeight(height = 40)

                Text(
                    text = "To get started, you just need a new server password, server socket port, and a place to put incoming files.",
                    style = paragraphStyle,
                )
                HMSpacerHeight(height = 3)
                Text(
                    text = "(You can change these later)",
                    style = psstStyle,
                    color = psstColour,
                )
                HMSpacerHeight(height = 15)
                Row {
                    Text(
                        text = "New server password",
                        style = inputLabelStyle,
                    )
                    // TODO: Help Icon
                }
                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                    textFieldState = serverPasswordState,
                )

                HMSpacerHeight(height = 15)

                Row {
                    Text(
                        text = "New server default port.",
                        style = inputLabelStyle,
                    )
                    // TODO: Help Icon
                }
                FittoniaNumberInput(
                    modifier = Modifier.fillMaxWidth(),
                    textFieldState = portFieldState,
                )

                HMSpacerHeight(height = 15)

                Row {
                    Text(
                        text = "Destination folder",
                        style = inputLabelStyle,
                    )
                    // TODO: Help Icon
                }
                FittoniaButton(
                    onClick = {
                        MainActivity.mainActivity.openDumpPicker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
                    },
                    content = {
                        ButtonText(text = "Select folder")
                    },
                )
            }
        },
        footer = {
            Footer {
                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = serverPasswordState.text.isNotEmpty() && portFieldState.text.toString().isNotEmpty(),
                    onClick = {
                        onContinue(
                            serverPasswordState.text.toString(),
                            portFieldState.text.toString().toInt(),
                        )
                    },
                    content = { ButtonText(text = "Continue") },
                )
            }
        },
    )
}
