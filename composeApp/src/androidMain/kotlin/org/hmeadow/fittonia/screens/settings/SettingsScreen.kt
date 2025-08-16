package org.hmeadow.fittonia.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.ReadOnlyEntries
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.architecture.currentStyle
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing2
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingSStyle
import org.hmeadow.fittonia.design.fonts.inputInputStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.utility.InfoBorderState.infoBoxOverlay

@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
    data: SettingsDataAndroid,
    onBackClicked: () -> Unit,
) {
    FittoniaScaffold(
        scrollable = false,
        header = { FittoniaHeader(includeInfoButton = true, onBackClicked = onBackClicked) },
        content = {
            Column(modifier = Modifier.padding(horizontal = spacing16)) {
                FittoniaSpacerHeight(height = spacing32)
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Settings",
                    style = headingLStyle,
                    textAlign = TextAlign.Center,
                )
                FittoniaSpacerHeight(height = spacing32)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = spacing2,
                            color = currentStyle.headerBackgroundColour,
                            shape = RoundedCornerShape(size = spacing8),
                        )
                        .padding(all = spacing16),
                ) {
                    Text(
                        text = "Update Access Code",
                        style = headingSStyle,
                    )
                    FittoniaSpacerHeight(height = spacing8)
                    FittoniaTextInput(
                        modifier = Modifier.fillMaxWidth(),
                        inputFlow = viewModel.serverAccessCodeState,
                    )
                    FittoniaSpacerHeight(height = spacing4)
                    FittoniaButton(onClick = viewModel::onUpdateAccessCode) {
                        ButtonText(text = "Save")
                        FittoniaSpacerWidth(width = spacing8)
                        ButtonIcon(drawableRes = R.drawable.ic_save)
                    }
                }

                FittoniaSpacerHeight(height = spacing32)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = spacing2,
                            color = currentStyle.headerBackgroundColour,
                            shape = RoundedCornerShape(size = spacing8),
                        )
                        .padding(all = spacing16),
                ) {
                    Text(
                        text = "Change Dump Location",
                        style = headingSStyle,
                    )
                    FittoniaSpacerHeight(height = spacing8)

                    ReadOnlyEntries(
                        entries = listOf(data.dumpPath.dumpPathReadable),
                        expandOnClick = true,
                        textStyle = inputInputStyle(color = Color(color = 0xFF000000)),
                    )

                    FittoniaSpacerHeight(height = spacing4)

                    FittoniaButton(
                        onClick = { MainActivity.mainActivity.openFolderPicker(viewModel::onDumpPathPicked) },
                        onInfo = {
                            Column {
                                Text(
                                    text = stringResource(R.string.welcome_screen_select_folder_button_info_1),
                                    style = paragraphTextStyle,
                                )
                                FittoniaSpacerHeight(height = spacing8)
                                Text(
                                    text = stringResource(R.string.welcome_screen_select_folder_button_info_2),
                                    style = paragraphTextStyle,
                                )
                                FittoniaSpacerHeight(height = spacing8)
                                Text(
                                    text = stringResource(R.string.welcome_screen_select_folder_button_info_3),
                                    style = paragraphTextStyle,
                                )
                            }
                        },
                        content = {
                            ButtonText(
                                text = stringResource(
                                    id = R.string.welcome_screen_new_destination_select_folder_button,
                                ),
                            )
                        },
                    )
                }
            }
        },
        footer = { },
        overlay = {
            infoBoxOverlay()
        },
    )
}
