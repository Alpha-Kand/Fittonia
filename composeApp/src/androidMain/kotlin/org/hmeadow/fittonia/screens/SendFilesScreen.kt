package org.hmeadow.fittonia.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.components.FittoniaBackground
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaButtonType
import org.hmeadow.fittonia.components.FittoniaComingSoon
import org.hmeadow.fittonia.components.FittoniaIcon
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaNumberInput
import org.hmeadow.fittonia.components.FittoniaTextInput
import org.hmeadow.fittonia.components.HMSpacerHeight
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.HorizontalLine
import org.hmeadow.fittonia.components.headingLStyle
import org.hmeadow.fittonia.components.headingMStyle
import org.hmeadow.fittonia.components.paragraphStyle
import org.hmeadow.fittonia.components.psstStyle

val destinations = listOf(
    "Home Computer",
    "Work Computer",
    "Bob's Computer",
    "One time destination",
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SendFilesScreen(
    onBackClicked: () -> Unit,
    onConfirmClicked: () -> Unit,
) {
    var fileList by remember { mutableStateOf<List<String>>(emptyList()) }
    var destinationPickerActive by remember { mutableStateOf(false) }
    var destinationState by remember { mutableStateOf("Select destination...") }
    FittoniaBackground(
        header = {
            Box {
                FittoniaIcon(
                    modifier = Modifier
                        .padding(5.dp)
                        .clickable(onClick = onBackClicked),
                    drawableRes = R.drawable.ic_back_arrow,
                )
                Row(Modifier.fillMaxWidth()) {
                    HMSpacerWeightRow()
                    Text(
                        text = "Send files",
                        style = headingLStyle,
                    )
                    HMSpacerWeightRow()
                }
            }
        },
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                HMSpacerHeight(height = 40)
                Text(
                    text = "1. Select files/folders to send.",
                    style = headingMStyle,
                )
                HMSpacerHeight(height = 5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 2.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE)),
                ) {
                    fileList.forEachIndexed { index, file ->
                        Row(
                            modifier = Modifier.padding(all = 5.dp),
                            verticalAlignment = CenterVertically,
                        ) {
                            Text(text = file)
                            HMSpacerWeightRow()
                            FittoniaIcon(
                                modifier = Modifier
                                    .requiredSize(14.dp)
                                    .clickable {
                                        fileList = fileList.filter { it != file }
                                    },
                                drawableRes = R.drawable.ic_clear,
                                tint = Color(0xFF222222),
                            )
                        }
                        if (index != fileList.lastIndex) {
                            HorizontalLine()
                        }
                    }
                }
                HMSpacerHeight(height = 5)
                Row {
                    FittoniaButton(
                        onClick = {
                            val alphabetRange = ('A'..'Z') + ('a'..'z')
                            fileList += (1..10).map { alphabetRange.random() }.joinToString(separator = "")
                        },
                        type = FittoniaButtonType.Secondary,
                        content = {
                            ButtonText(text = "Add")
                            HMSpacerWidth(width = 5)
                            ButtonIcon(drawableRes = R.drawable.ic_add)
                        },
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                }
                HMSpacerHeight(height = 40)
                Text(
                    text = "2. Destination",
                    style = headingMStyle,
                )
                HMSpacerHeight(height = 5)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = Color(0xFF446644))
                        .background(color = Color(0xFFDDFFEE))
                        .padding(5.dp)
                        .clickable(onClick = { destinationPickerActive = true }),
                ) {
                    Text(
                        text = destinationState,
                    )
                    HMSpacerWeightRow()
                    FittoniaIcon(
                        modifier = Modifier.requiredSize(10.dp),
                        drawableRes = R.drawable.ic_chevron_down,
                        tint = Color(0xFF222222),
                    )
                }
                HMSpacerHeight(height = 30)
                Text(
                    text = "3. IP Address/Code",
                    style = headingMStyle,
                )
                HMSpacerHeight(height = 5)
                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                )
                HMSpacerHeight(height = 30)
                Text(
                    text = "4. Port",
                    style = headingMStyle,
                )
                HMSpacerHeight(height = 5)
                FittoniaNumberInput(
                    modifier = Modifier.fillMaxWidth(),
                )
                HMSpacerHeight(height = 30)
                Row {
                    Text(
                        text = "5. Description",
                        style = headingMStyle,
                    )
                    HMSpacerWidth(width = 4)
                    Text(
                        modifier = Modifier.align(alignment = CenterVertically),
                        text = "(optional)",
                        style = psstStyle,
                    )
                }
                HMSpacerHeight(height = 5)
                FittoniaTextInput(
                    modifier = Modifier.fillMaxWidth(),
                )
                HMSpacerHeight(height = 5)
                FittoniaComingSoon {
                    FittoniaButton(
                        onClick = { },
                        type = FittoniaButtonType.Secondary,
                        content = {
                            ButtonText(text = "Save destination")
                            HMSpacerWidth(width = 5)
                            ButtonIcon(drawableRes = R.drawable.ic_save)
                        },
                    )
                }
            }
        },
        footer = {
            FittoniaButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp),
                content = { ButtonText(text = "Confirm") },
                onClick = onConfirmClicked,
            )
        },
        overlay = {
            FittoniaModal(
                state = destinationPickerActive,
                onDismiss = { destinationPickerActive = false },
            ) {
                listOf(
                    Options(
                        name = "About",
                        onClick = {},
                    ),
                ).fastForEach {
                    destinations.forEachIndexed { index, destination ->
                        Row(
                            modifier = Modifier.clickable {
                                destinationState = destination
                                destinationPickerActive = false
                            },
                        ) {
                            Text(
                                text = destination,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                style = if (index != destinations.lastIndex) {
                                    paragraphStyle
                                } else {
                                    TextStyle(
                                        fontSize = 17.sp,
                                        lineHeight = 23.sp,
                                        letterSpacing = (-0.2f).sp,
                                        fontStyle = FontStyle.Italic,
                                    )
                                },
                            )
                            HMSpacerWeightRow()
                            FittoniaIcon(
                                modifier = Modifier
                                    .requiredSize(10.dp)
                                    .align(CenterVertically),
                                drawableRes = R.drawable.ic_chevron_right,
                                tint = Color(0xFF222222),
                            )
                        }

                        if (index != destinations.lastIndex) {
                            HorizontalLine()
                        }
                    }
                }
            }
        },
    )
}

@Composable
@Preview
private fun Preview() {
    SendFilesScreen(
        onBackClicked = { },
        onConfirmClicked = { },
    )
}
