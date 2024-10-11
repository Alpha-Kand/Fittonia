package org.hmeadow.fittonia.screens.debugScreen

import SettingsManager
import android.os.Build
import android.os.VibrationEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hmeadow.fittonia.BaseViewModel
import org.hmeadow.fittonia.MainActivity
import org.hmeadow.fittonia.MainViewModel
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.SettingsDataAndroid
import org.hmeadow.fittonia.UserAlert
import org.hmeadow.fittonia.components.FittoniaButton
import org.hmeadow.fittonia.components.FittoniaHeader
import org.hmeadow.fittonia.components.FittoniaModal
import org.hmeadow.fittonia.components.FittoniaScaffold
import org.hmeadow.fittonia.components.Footer
import org.hmeadow.fittonia.components.HMSpacerWeightRow
import org.hmeadow.fittonia.components.HMSpacerWidth
import org.hmeadow.fittonia.components.InputFlow
import org.hmeadow.fittonia.utility.rememberSuspendedAction
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.random.Random

class DebugScreenViewModel(
    private val mainViewModel: MainViewModel,
) : BaseViewModel() {
    val deviceIp = MutableStateFlow("Unknown")

    // Defaults
    val defaultNewDestinationName = InputFlow(initial = "")
    val defaultNewDestinationPort = InputFlow(initial = "")
    val defaultNewDestinationPassword = InputFlow(initial = "")
    val defaultNewDestinationIP = InputFlow(initial = "")
    val needToSave = combine(
        mainViewModel.dataStore.data,
        defaultNewDestinationPort,
    ) { data, newDefaultDestinationPort ->
        newDefaultDestinationPort != data.debugSettings.defaultNewDestinationPort.toString()
    }

    // Admin Create
    val nextAutoJobName = mainViewModel.dataStore.data.map { it.nextAutoJobName }
    val nextAutoJobNameMessage = MutableStateFlow("")

    init {
        launch {
            mainViewModel.dataStore.data.first().run {
                defaultNewDestinationName.value = debugSettings.defaultNewDestinationName
                defaultNewDestinationPort.value = debugSettings.defaultNewDestinationPort.toString()
                defaultNewDestinationIP.value = debugSettings.defaultNewDestinationIP
                defaultNewDestinationPassword.value = debugSettings.defaultNewDestinationPassword
            }
        }
        refreshIp()
    }

    fun refreshIp() {
        deviceIp.value = MainActivity.mainActivity.getDeviceIpAddress() ?: "Unknown"
    }

    fun createJobDirectory() {
        launch {
            val expectedJobNumber = nextAutoJobName.first()
            when (MainActivity.mainActivity.createJobDirectory(jobName = null, print = ::println)) {
                is MainActivity.CreateDumpDirectory.Success -> {
                    nextAutoJobNameMessage.update {
                        nextAutoJobName.first().let { nextAutoJobName ->
                            if (nextAutoJobName != expectedJobNumber + 1) {
                                "Success! Job$expectedJobNumber already existed so created Job${nextAutoJobName - 1} instead."
                            } else {
                                "Success! Created Job${nextAutoJobName - 1}"
                            }
                        }
                    }
                }

                else -> nextAutoJobNameMessage.update { "Error!" }
            }
        }
    }

    fun createNewDestination() {
        val getIpNum = { abs(Random.nextInt() % 256) }
        mainViewModel.addDestination(
            destination = SettingsManager.Destination(
                name = "${defaultNewDestinationName.value} ${abs(Random.nextInt() % 100)}",
                ip = "${getIpNum()}.${getIpNum()}.${getIpNum()}.${getIpNum()}",
                password = defaultNewDestinationPassword.value,
            ),
        )
    }

    fun onSaveDefaults() {
        launch {
            mainViewModel.dataStore.updateData {
                it.copy(
                    debugSettings = it.debugSettings.copy(
                        defaultNewDestinationPort = defaultNewDestinationPort.value.toIntOrNull()
                            ?: it.debugSettings.defaultNewDestinationPort,
                    ),
                )
            }
        }
    }
}

@Composable
fun DebugScreen(
    viewModel: DebugScreenViewModel,
    data: SettingsDataAndroid,
    onResetSettingsClicked: () -> Unit,
    onClearDumpPath: () -> Unit,
    onBackClicked: () -> Unit,
    debugNewThread: suspend () -> Unit,
    debugNewDestination: () -> Unit,
    tabScrollState: LazyListState = rememberLazyListState(),
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
        content = {
            var lastFocusedTab by remember { mutableStateOf<LazyListItemInfo?>(null) }
            var lock by remember { mutableStateOf(false) }
            var targetIndex by remember { mutableIntStateOf(0) }
            var lastIndex by remember { mutableIntStateOf(0) }
            var indicatorOffset by remember { mutableFloatStateOf(0f) }

            var currentIndex by remember { mutableIntStateOf(0) }
            var capturedIndex by remember { mutableIntStateOf(-1) }
            var nextIndex by remember { mutableIntStateOf(0) }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    /*.draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState {
                            //println("drag = $it")
                        },
                        onDragStopped = {},
                    )*/
                    .nestedScroll(
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                if (tabScrollState.layoutInfo.visibleItemsInfo.size > 1) {
                                    if (available.x > 0f) {
                                        currentIndex = tabScrollState.layoutInfo.visibleItemsInfo[1].index
                                        if(capturedIndex == -1) capturedIndex = currentIndex
                                        nextIndex = tabScrollState.layoutInfo.visibleItemsInfo[0].index
                                    } else if (available.x < 0f) {
                                        currentIndex = tabScrollState.layoutInfo.visibleItemsInfo[0].index
                                        if(capturedIndex == -1) capturedIndex = currentIndex
                                        nextIndex = tabScrollState.layoutInfo.visibleItemsInfo[1].index
                                    }
                                }
                                if (!lock) {
                                    lock = true
                                    // Determine which tab is in focus by how 'extreme' the offset is.
                                    lastFocusedTab = tabScrollState
                                        .layoutInfo
                                        .visibleItemsInfo
                                        .minByOrNull { it.offset.absoluteValue }
                                }
                                indicatorOffset -= available.x
                                println("indicatorOffset $indicatorOffset")
                                return super.onPreScroll(available, source)
                            }

                            override suspend fun onPreFling(available: Velocity): Velocity {
                                if (available.y.absoluteValue > available.x.absoluteValue && available.x.absoluteValue < 2000f) {
                                    return super.onPreFling(available)
                                }
                                lock = false
                                val new = tabScrollState.layoutInfo.visibleItemsInfo.firstOrNull {
                                    it.index == lastFocusedTab?.index
                                }
                                val index = if (new != null) {
                                    if (new.offset.absoluteValue > 200 || available.x.absoluteValue > 1000f) {
                                        new.index - new.offset.sign
                                    } else {
                                        tabScrollState
                                            .layoutInfo
                                            .visibleItemsInfo
                                            .minByOrNull { it.offset.absoluteValue }
                                            ?.index ?: 0
                                    }
                                } else {
                                    tabScrollState
                                        .layoutInfo
                                        .visibleItemsInfo
                                        .minByOrNull { it.offset.absoluteValue }
                                        ?.index ?: 0
                                }
                                //println("index = $index")
                                lastIndex = targetIndex
                                targetIndex = index
                                currentIndex = -1
                                capturedIndex = -1
                                indicatorOffset = 0f
                                tabScrollState.animateScrollToItem(
                                    index = index,
                                    scrollOffset = 0,
                                )
                                return available
                            }
                        },
                    ),
            ) {
                var selectedIndexWidth by remember { mutableStateOf(0.dp) }
                SubcomposeLayout { constraints ->
                    val aaa = listOf("Overview", "Defaultsss", "Admin Createeeeee")
                    var textHeight: Dp = 0.dp

                    val tabNames = aaa.mapIndexed { index, name ->
                        subcompose(index) {
                            Text(
                                text = name,
                                modifier = Modifier
                                    .border(width = 1.dp, color = Color.Cyan)
                                    //.padding(all = 6.dp)
                                    .clickable { targetIndex = index },
                            )
                        }.single().measure(constraints).also {
                            textHeight = maxOf(textHeight, it.height.toDp())
                            if (index == targetIndex && indicatorOffset == 0f && capturedIndex == -1) {
                                selectedIndexWidth = it.width.toDp()
                            }
                        }
                    }
                    if (currentIndex >= 0 && capturedIndex >= 0) {
                        val currentSmaller = tabNames[currentIndex].width < tabNames[nextIndex].width
                        val minSize = min(tabNames[currentIndex].width, tabNames[nextIndex].width)
                        val maxSize = max(tabNames[currentIndex].width, tabNames[nextIndex].width)
                        val diffSize = abs(tabNames[currentIndex].width - tabNames[nextIndex].width)
                        val currentWidth = (tabNames[capturedIndex].width + (diffSize * (indicatorOffset / constraints.maxWidth)))
                        /*
                        val currentWidth = if(currentSmaller) {
                            (minSize + (diffSize * (indicatorOffset / constraints.maxWidth)))
                        } else {
                            (maxSize - (diffSize * (indicatorOffset / constraints.maxWidth)))
                        }
                         */
                        selectedIndexWidth = currentWidth.toDp()
                        println("capturedIndex = $capturedIndex")
                        /*
                        println("----")
                        println("tabNames[currentIndex].width ${tabNames[currentIndex].width}    tabNames[nextIndex].width ${tabNames[nextIndex].width}")
                        println("minSize = $minSize")
                        println("maxSize = $maxSize")
                        println("diffSize = $diffSize")
                        println("currentWidth = $currentWidth")
                        println("----")
                         */
                    }

                    /*if(currentIndex in 0..<nextIndex){
                        selectedIndexWidth = min(tabNames[currentIndex].width, tabNames[nextIndex].width).toDp() + abs(tabNames[currentIndex].width - tabNames[nextIndex].width).toDp()
                    }else if(currentIndex >= 0 && nextIndex in 0..<currentIndex){
                        selectedIndexWidth = min(tabNames[currentIndex].width, tabNames[nextIndex].width).toDp() + abs(tabNames[currentIndex].width - tabNames[nextIndex].width).toDp()
                    }*/

                    val indicator = subcompose(aaa.size) {
                        Box(
                            modifier = Modifier
                                .width(selectedIndexWidth)
                                .height(4.dp)
                                .background(
                                    color = Color.Black,
                                    //shape = RoundedCornerShape(topStartPercent = 100, topEndPercent = 100),
                                ),
                        )
                    }.single().measure(constraints)
                    val indicator2 = subcompose(aaa.size + 1) {
                        Box(
                            modifier = Modifier
                                .width(selectedIndexWidth)
                                .height(8.dp)
                                .background(
                                    color = Color.Cyan,
                                ),
                        )
                    }.single().measure(constraints)

                    layout(constraints.maxWidth, constraints.minHeight) {
                        var tabNameOffset = 0
                        tabNames.forEachIndexed { index, it ->
                            it.placeRelative(tabNameOffset, 0)
                            if (index == targetIndex) {
                                // OLD BLACK JUMPING INDICATOR indicator.placeRelative(tabNameOffset, textHeight.toPx().roundToInt())
                            }
                            var ccc = 0
                            if (targetIndex > 0) {
                                ccc = (0..<targetIndex).sumOf { tabNames[it].width }
                            }
                            if (index == targetIndex && currentIndex == -1) {
                                indicator2.placeRelative(tabNameOffset, (textHeight.toPx() - 2).roundToInt())
                            } else if (index == currentIndex && currentIndex < nextIndex) {
                                val bbb =
                                    ceil((indicatorOffset / constraints.maxWidth) * tabNames[index].width).roundToInt()
                                indicator2.placeRelative(bbb + ccc, (textHeight.toPx() - 2).roundToInt())
                            } else if (index == currentIndex && currentIndex > nextIndex) {
                                val bbb =
                                    ceil((indicatorOffset / constraints.maxWidth) * tabNames[index - 1].width).roundToInt()
                                indicator2.placeRelative(bbb + ccc, (textHeight.toPx() - 2).roundToInt())
                            }
                            tabNameOffset += it.width
                        }
                    }
                }
                LaunchedEffect(tabScrollState, targetIndex) {
                    snapshotFlow { tabScrollState }
                        .filter { targetIndex >= 0 }
                        .collect {
                            withContext(NonCancellable) {
                                currentIndex = -1
                                capturedIndex = -1
                                indicatorOffset = 0f
                                tabScrollState.animateScrollToItem(index = targetIndex, scrollOffset = 0)
                            }
                        }
                }

                LazyRow(
                    state = tabScrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),
                ) {
                    item {
                        DebugScreenOverviewTab(
                            modifier = Modifier
                                .width(maxWidth),//.height(maxHeight),
                            viewModel = viewModel,
                            data = data,
                            onClearDumpPath = onClearDumpPath,
                        )
                    }
                    item {
                        DebugScreenDefaultsTab(
                            defaultNewDestinationName = viewModel.defaultNewDestinationName,
                            defaultNewDestinationPort = viewModel.defaultNewDestinationPort,
                            defaultNewDestinationPassword = viewModel.defaultNewDestinationPassword,
                            defaultNewDestinationIP = viewModel.defaultNewDestinationIP,
                            onSaveDefaults = viewModel::onSaveDefaults,
                            needToSave = viewModel.needToSave.collectAsState(initial = false).value,
                            modifier = Modifier
                                .width(maxWidth),//.height(maxHeight),
                        )
                    }
                    item {
                        DebugScreenAdminCreateTab(
                            nextAutoJobName = viewModel.nextAutoJobName.collectAsState(initial = -1).value,
                            nextAutoJobNameMessage = viewModel.nextAutoJobNameMessage.collectAsState(initial = "").value,
                            onCreateNewDestination = viewModel::createNewDestination,
                            onCreateJobDirectory = viewModel::createJobDirectory,
                            modifier = Modifier
                                .width(maxWidth)
                                .height(maxHeight),
                        )
                    }
                }
            }
            /*
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Text(
                    text = "MainActivity/MainViewModel",
                    style = headingSStyle,
                )
                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { debugAlertsState = true },
                ) {
                    ButtonText(text = "Alerts")
                }
                FittoniaTextInput(
                    inputFlow = viewModel.newDumpDirectory,
                    label = "New Dump Directory",
                )
                FittoniaButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::createDumpDirectory,
                ) {
                    ButtonText(text = "Create dump directory")
                }
                Text(
                    text = "Destinations",
                    style = headingSStyle,
                )
                data.destinations.forEach { destination ->
                    Row(
                        modifier = Modifier.background(color = Color.LightGray),
                        verticalAlignment = CenterVertically,
                    ) {
                        Column {
                            Text(text = "Name: ${destination.name}")
                            Text(text = "IP: ${destination.ip}")
                            Text(text = "Password: ${destination.password}")
                        }

                        HMSpacerWeightRow()

                        FittoniaIcon(
                            modifier = Modifier
                                .requiredSize(20.dp)
                                .clickable { onRemoveDestinationClicked(destination) },
                            drawableRes = R.drawable.ic_clear,
                        )
                    }
                    HMSpacerHeight(height = 10)
                }
            }
            */
        },
        footer = {
            Footer {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FittoniaButton(onClick = viewModel.rememberSuspendedAction(debugNewThread)) {
                            ButtonText(text = "New Thread")
                        }
                        HMSpacerWeightRow()
                        FittoniaButton(onClick = debugNewDestination) {
                            ButtonText(text = "New Destination")
                        }
                    }
                    FittoniaButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onResetSettingsClicked,
                    ) {
                        ButtonText(text = "Reset Settings")
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
                        HMSpacerWidth(width = 5)
                        ButtonText(text = "UserAlert.PortInUse")
                    }
                }
            }
        },
    )
}
