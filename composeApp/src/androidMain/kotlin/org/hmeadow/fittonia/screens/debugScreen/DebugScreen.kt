package org.hmeadow.fittonia.screens.debugScreen

import SettingsManager
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.components.ButtonIcon
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.pager.LazyPager
import org.hmeadow.fittonia.components.pager.PagerState
import org.hmeadow.fittonia.components.pager.PagerState.Companion.rememberPagerState
import org.hmeadow.fittonia.components.pager.PagerTabLabels
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing4
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.mainActivity.MainActivity

@Composable
internal fun DebugScreen(
    viewModel: DebugScreenViewModel,
    data: SettingsDataAndroid,
    onResetSettingsClicked: () -> Unit,
    onResetColours: () -> Unit,
    onClearDumpPath: () -> Unit,
    onRemoveDestinationClicked: (SettingsManager.Destination) -> Unit,
    onBackClicked: () -> Unit,
    debugNewDestination: () -> Unit,
    saveColours: () -> Unit,
    cancelColourChanges: () -> Unit,
    pagerState: PagerState = rememberPagerState(),
) {
    var debugAlertsState by remember { mutableStateOf(false) }
    FittoniaScaffold(
        scrollable = false,
        header = {
            FittoniaHeader(
                headerText = "Debug Screen",
                onBackClicked = onBackClicked,
            )
        },
        content = { footerHeight ->
            val debugTabs: List<Pair<String, @Composable (maxWidth: Dp, maxHeight: Dp) -> Unit>> =
                listOf(
                    "App's Paint Job" to { maxWidth, maxHeight ->
                        DebugScreenPaintJobTab(
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            onResetColours = onResetColours,
                            footerHeight = footerHeight,
                            viewModel = viewModel,
                        )
                    },
                    "Overview" to { maxWidth, maxHeight ->
                        DebugScreenOverviewTab(
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            viewModel = viewModel,
                            footerHeight = footerHeight,
                        )
                    },
                    "Data Store" to { maxWidth, maxHeight ->
                        DebugScreenDataStoreTab(
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            data = data,
                            onClearDumpPath = onClearDumpPath,
                            onRemoveDestinationClicked = onRemoveDestinationClicked,
                            footerHeight = footerHeight,
                        )
                    },
                    "Defaults" to { maxWidth, maxHeight ->
                        DebugScreenDefaultsTab(
                            defaultSendThrottle = viewModel.defaultSendThrottle,
                            defaultNewDestinationName = viewModel.defaultNewDestinationName,
                            defaultNewDestinationPort = viewModel.defaultNewDestinationPort,
                            defaultNewDestinationAccessCode = viewModel.defaultNewDestinationAccessCode,
                            defaultNewDestinationIP = viewModel.defaultNewDestinationIP,
                            onSaveDefaults = viewModel::onSaveDefaults,
                            needToSave = viewModel.needToSave.collectAsState(initial = false).value,
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            footerHeight = footerHeight,
                        )
                    },
                    "Admin Create" to { maxWidth, maxHeight ->
                        DebugScreenAdminCreateTab(
                            nextAutoJobName = viewModel.nextAutoJobName.collectAsState(initial = -1).value,
                            nextAutoJobNameMessage = viewModel
                                .nextAutoJobNameMessage
                                .collectAsState(initial = "")
                                .value,
                            onCreateNewDestination = viewModel::createNewDestination,
                            onCreateJobDirectory = viewModel::createJobDirectory,
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            footerHeight = footerHeight,
                        )
                    },
                    "Encryption Test" to { maxWidth, maxHeight ->
                        DebugScreenEncryptionTestTab(
                            // Public/Private Key
                            maxEncryptionBytesPuPr = viewModel.maxEncryptionBytesPuPr,
                            encodedPuPr = viewModel.encryptedMessagePuPr.collectAsState(ByteArray(0)).value,
                            decodedPuPr = viewModel.decryptedMessagePuPr.collectAsState("").value,
                            onEncryptMessagePuPr = viewModel::onEncryptMessagePuPr,
                            onDecryptMessagePuPr = viewModel::onDecryptMessagePuPr,
                            // AES Encryption
                            maxEncryptionBytesAES = viewModel.maxEncryptionBytesAES,
                            encodedAES = viewModel.encryptedMessageAES.collectAsState(ByteArray(0)).value,
                            decodedAES = viewModel.decryptedMessageAES.collectAsState("").value,
                            onEncryptMessageAES = viewModel::onEncryptMessageAES,
                            onDecryptMessageAES = viewModel::onDecryptMessageAES,
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                            footerHeight = footerHeight,
                        )
                    },
                )

            BoxWithConstraints {
                val box = this
                Column {
                    PagerTabLabels(
                        position = pagerState.pagePosition,
                        tabs = remember(debugTabs) { debugTabs.map { it.first } },
                        onTabSelected = pagerState::goToPage,
                        modifier = Modifier.padding(bottom = spacing16),
                    )
                    LazyPager(
                        pagerState = pagerState,
                        spacing = spacing16,
                    ) {
                        items(debugTabs) {
                            it.second.invoke(box.maxWidth, box.maxHeight)
                        }
                    }
                }
            }
        },
        footer = {
            Footer {
                if (pagerState.targetPage == 0) {
                    Row {
                        FittoniaButton(onClick = saveColours) { ButtonText("Save Colours") }
                        FittoniaSpacerWidth(width = spacing8)
                        FittoniaButton(onClick = cancelColourChanges) { ButtonText("Cancel Changes") }
                    }
                }
                if (pagerState.targetPage == 2) {
                    Column {
                        FittoniaButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = debugNewDestination,
                        ) {
                            ButtonText(text = "New Destination")
                        }

                        FittoniaButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onResetSettingsClicked,
                        ) {
                            ButtonText(text = "Reset Settings (except colours)")
                        }
                    }
                }
            }
        },
        overlay = {
            FittoniaModal(
                state = debugAlertsState,
                onDismiss = { debugAlertsState = false },
            ) { _ ->
                Column {
                    FittoniaButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            MainActivity.mainActivityForServer?.alert(UserAlert.PortInUse(port = 42069))
                        },
                    ) {
                        ButtonIcon(drawableRes = R.drawable.ic_add)
                        FittoniaSpacerWidth(width = spacing4)
                        ButtonText(text = "UserAlert.PortInUse")
                    }
                }
            }
        },
    )
}
