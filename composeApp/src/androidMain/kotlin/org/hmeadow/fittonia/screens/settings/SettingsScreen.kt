package org.hmeadow.fittonia.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaScaffold
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

@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
    onBackClicked: () -> Unit,
) {
    FittoniaScaffold(
        scrollable = false,
        header = { FittoniaHeader(onBackClicked = onBackClicked) },
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
            }
        },
        footer = { },
    )
}
