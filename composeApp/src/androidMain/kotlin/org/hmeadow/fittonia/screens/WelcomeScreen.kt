package org.hmeadow.fittonia.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.MainViewModel
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.ReadOnlyEntries
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.compose.components.FittoniaTextInput
import org.hmeadow.fittonia.compose.components.InputFlow
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing2
import org.hmeadow.fittonia.design.Spacing.spacing32
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.inputInputStyle
import org.hmeadow.fittonia.design.fonts.inputLabelStyle
import org.hmeadow.fittonia.design.fonts.paragraphParagraphStyle
import org.hmeadow.fittonia.design.fonts.paragraphSpanStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.design.fonts.psstStyle
import org.hmeadow.fittonia.utility.Debug
import org.hmeadow.fittonia.utility.InfoBorderState
import org.hmeadow.fittonia.utility.InfoBorderState.infoBoxOverlay
import org.hmeadow.fittonia.utility.Off

class WelcomeScreenViewModel(
    private val mainViewModel: MainViewModel,
    private val onContinueCallback: (accessCode: String, port: Int) -> Unit,
) : BaseViewModel() {
    val serverAccessCodeState = InputFlow(initial = "")

    val canContinue = combine(
        serverAccessCodeState,
        mainViewModel.dataStore.data,
    ) { accessCodeState, dumpPathState ->
        accessCodeState.isNotEmpty() && dumpPathState.dumpPath.dumpUriPath.isNotEmpty()
    }

    fun onContinue() {
        onContinueCallback(
            serverAccessCodeState.text,
            44556, // TODO - after release somewhere to put constants.
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
        header = { FittoniaHeader(includeInfoButton = true) },
        content = {
            Column(modifier = Modifier.padding(horizontal = spacing16)) {
                FittoniaSpacerHeight(height = spacing32)

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.welcome_screen_title),
                    style = headingLStyle,
                    textAlign = TextAlign.Center,
                )

                Debug(Off) {
                    FittoniaButton(
                        onClick = {
                            viewModel.serverAccessCodeState.text = "accesscode"
                        },
                    ) {
                        ButtonText(text = "<Debug Fill Values>")
                    }
                }

                FittoniaSpacerHeight(height = spacing32)

                Text(
                    text = stringResource(R.string.welcome_screen_body),
                    style = paragraphTextStyle,
                )

                FittoniaSpacerHeight(height = spacing8)

                Text(
                    text = stringResource(id = R.string.welcome_screen_you_can_change_these_later),
                    style = psstStyle,
                )

                FittoniaSpacerHeight(height = spacing32)

                AccessCodeField(
                    modifier = Modifier.fillMaxWidth(),
                    serverAccessCodeState = viewModel.serverAccessCodeState,
                )

                FittoniaSpacerHeight(height = spacing32)

                Text(
                    text = stringResource(id = R.string.welcome_screen_new_destination_folder_label),
                    style = inputLabelStyle,
                )
                FittoniaSpacerHeight(height = spacing8)
                ReadOnlyEntries(
                    entries = listOf(data.dumpPath.dumpPathReadable),
                    onEntryClearClicked = { onClearDumpPath() },
                    expandOnClick = false,
                    textStyle = inputInputStyle(color = Color(color = 0xFF000000)),
                )

                FittoniaSpacerHeight(height = spacing16)

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
                            text = stringResource(id = R.string.welcome_screen_new_destination_select_folder_button),
                        )
                    },
                )

                FittoniaSpacerHeight(height = spacing32)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccessCodeField(serverAccessCodeState: InputFlow, modifier: Modifier = Modifier) {
    FittoniaTextInput(
        modifier = modifier,
        inputFlow = serverAccessCodeState,
        onInfo = accessCodeFieldInfo,
        label = {
            FlowRow {
                Text(
                    modifier = Modifier
                        .padding(end = spacing16)
                        .align(alignment = Alignment.CenterVertically),
                    text = stringResource(id = R.string.welcome_screen_new_server_access_code_label),
                    style = inputLabelStyle,
                )
                val interactionSource = remember { MutableInteractionSource() }
                Text(
                    modifier = Modifier
                        .padding(end = spacing2)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(),
                            onClick = {
                                InfoBorderState.enableInfoBorder()
                                InfoBorderState.infoBox = accessCodeFieldInfo
                            },
                        )
                        .align(alignment = Alignment.CenterVertically),
                    text = "(What's an access code?)",
                    color = Color(color = 0xFF003CCF),
                    style = psstStyle,
                )

                FittoniaIcon(
                    modifier = Modifier
                        .requiredSize(size = 25.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(),
                            onClick = {
                                InfoBorderState.enableInfoBorder()
                                InfoBorderState.infoBox = accessCodeFieldInfo
                            },
                        )
                        .align(alignment = Alignment.CenterVertically),
                    drawableRes = R.drawable.ic_info,
                    tint = Color(color = 0xFF003CCF),
                )
            }
        },
    )
}

private val accessCodeFieldInfo: @Composable () -> Unit = {
    val info1 = stringResource(R.string.welcome_screen_access_code_field_info_1)
    val info2 = stringResource(R.string.welcome_screen_access_code_field_info_2)
    val info3 = stringResource(R.string.welcome_screen_access_code_field_info_3)
    val paraSpanStyle = paragraphSpanStyle
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
    FittoniaSpacerHeight(height = spacing8)
    Text(
        text = stringResource(R.string.welcome_screen_access_code_field_info_4),
        style = paragraphTextStyle,
    )
}
