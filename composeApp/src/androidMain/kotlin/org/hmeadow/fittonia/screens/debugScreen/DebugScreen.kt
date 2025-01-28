package org.hmeadow.fittonia.screens.debugScreen

import SettingsManager
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
import kotlin.math.roundToInt
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
        newDefaultDestinationPort.text != data.debugSettings.defaultNewDestinationPort.toString()
    }

    // Admin Create
    val nextAutoJobName = mainViewModel.dataStore.data.map { it.nextAutoJobName }
    val nextAutoJobNameMessage = MutableStateFlow("")

    init {
        launch {
            mainViewModel.dataStore.data.first().run {
                defaultNewDestinationName.string = debugSettings.defaultNewDestinationName
                defaultNewDestinationPort.string = debugSettings.defaultNewDestinationPort.toString()
                defaultNewDestinationIP.string = debugSettings.defaultNewDestinationIP
                defaultNewDestinationPassword.string = debugSettings.defaultNewDestinationPassword
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
                password = defaultNewDestinationPassword.string,
            ),
        )
    }

    fun onSaveDefaults() {
        launch {
            mainViewModel.dataStore.updateData {
                it.copy(
                    debugSettings = it.debugSettings.copy(
                        defaultNewDestinationPort = defaultNewDestinationPort.string.toIntOrNull()
                            ?: it.debugSettings.defaultNewDestinationPort,
                    ),
                )
            }
        }
    }
}

fun LazyListItemInfo?.nextItem(visibleItemsInfoState: List<LazyListItemInfo>): LazyListItemInfo? =
    this?.let { leftMost ->
        visibleItemsInfoState.find { it.index == leftMost.index + 1 }
    }

fun Float.clampScrollLimits(maxScroll: Float): Float = maxOf(maxScroll, minOf(0f, this))

const val MIN_SCREEN_PERCENT_REQUIRED_TO_SCROLL = 0.125
const val MIN_VELOCITY_REQUIRED_TO_SCROLL = 1000f
val INDICATOR_HEIGHT = 8.dp

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
    val DEBUGtabNameStrings = listOf(
        "Overview",
        "Defaultsssdddddddddd",
        "Admin Create",
        "Duplicate 1",
        "Duplicate 2",
        "Duplicate 3",
        "Duplicate 4",
        "Duplicate 5",
    )

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
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~
            // ~~~~~  CONTENT TABS  ~~~~~
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~

            /** Compose safe access to 'visibleItemsInfo'. */
            val visibleItemsInfoState = remember { derivedStateOf { tabScrollState.layoutInfo.visibleItemsInfo } }.value

            /** The left-most visible item at any given time. */
            val leftMostVisibleItem = remember {
                derivedStateOf { tabScrollState.layoutInfo.visibleItemsInfo.minByOrNull { it.offset } }
            }.value

            /** How much the user scrolls per drag/swipe. Resets on finger up. */
            var swipeOffset by remember { mutableFloatStateOf(0f) }

            /**
             * Direction the tabs are being animated-scrolling.
             * -1 = scrolling to next tab, 1 = moving to previous tab.
             */
            var scrollingDirection by remember { mutableFloatStateOf(0f) }

            /** Index of the tab the user should currently see. */
            var currentTabIndex by remember { mutableIntStateOf(0) }

            /**
             * Index of the tab the user last saw. Not updated if the user jumped to a new tab by clicking on its name.
             */
            var previousTabIndex by remember { mutableIntStateOf(0) }

            /** Index of the tab the user is jumping to by clicking on its name. */
            var jumpingToTabIndex by remember { mutableIntStateOf(-1) }

            /** Whether the user is currently jumping to a new tab or not. */
            val isJumpingToNewTab = remember { derivedStateOf { jumpingToTabIndex != -1 } }.value

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~
            // ~~~~~ TAB NAMES VIEW ~~~~~
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~

            /**
             * Tab names scroll offset. Affected by user scrolling the tab names or scrolling tabs until indicator
             * reaches edges of the screen.
             */
            var tabNamesScrollOffset by remember { mutableFloatStateOf(0f) }

            /**
             * How much the tabs name view is allowed to scroll to the right at any given moment. Minimum scroll is
             * assumed to be 0. Limits how far the user can scroll and informs auto-scrolling its scroll limit.
             */
            var maxTabNamesScrollLimit by remember { mutableFloatStateOf(0f) }

            /**
             * Flag for when the tabs view is scrolling due to reasons other than direct user interaction, such as
             * swiping tabs normally when the indicator is near the edges of the screen.
             */
            var isTabNamesAutoScrolling by remember { mutableStateOf(false) }

            /** How much the tabs view is currently auto-scrolling by. Controlled by an [animateIntAsState]. */
            var tabNamesAutoScrollingValue by remember { mutableIntStateOf(0) }

            /** Animation for when the tabs view is auto-scrolling. */
            val tabNamesAutoScrollerAnimator = animateIntAsState(
                targetValue = tabNamesAutoScrollingValue,
                finishedListener = {
                    if (isTabNamesAutoScrolling) {
                        isTabNamesAutoScrolling = false
                        val lastScrollOffset = (scrollingDirection * tabNamesAutoScrollingValue)
                        tabNamesScrollOffset = (tabNamesScrollOffset.roundToInt() + lastScrollOffset).clampScrollLimits(
                            maxScroll = maxTabNamesScrollLimit,
                        )
                        tabNamesAutoScrollingValue = 0
                    }
                },
                label = "Tabs View Auto-Scroller Animator",
            )
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            var triggerTabNamesAutoScroll by remember { mutableStateOf(false) }
            var screenWidthPx by remember { mutableFloatStateOf(0f) }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                val visibleItemsInfo = tabScrollState.layoutInfo.visibleItemsInfo
                                if (visibleItemsInfo.size <= 1) return super.onPreScroll(available, source)
                                swipeOffset -= available.x
                                return super.onPreScroll(available, source)
                            }

                            override suspend fun onPreFling(available: Velocity): Velocity {
                                val isVerticallyScrolling = available.y.absoluteValue > available.x.absoluteValue
                                if (isVerticallyScrolling) return super.onPreFling(available)

                                val leftMostVisibleItemIndex = leftMostVisibleItem?.index ?: 0
                                val rightMostVisibleItemIndex = leftMostVisibleItem
                                    ?.nextItem(visibleItemsInfoState = visibleItemsInfoState)
                                    ?.index
                                    ?: return super.onPreFling(available) // Don't scroll past end of tabs.
                                val isScrollingHardEnough = available.x.absoluteValue > MIN_VELOCITY_REQUIRED_TO_SCROLL
                                val screenScrolledPercent = screenWidthPx * MIN_SCREEN_PERCENT_REQUIRED_TO_SCROLL
                                val hasScrolledFarEnough = abs(swipeOffset) > screenScrolledPercent
                                if (isScrollingHardEnough || hasScrolledFarEnough) {
                                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                                    // Scroll to new tab.
                                    when {
                                        // Moving right.
                                        swipeOffset > 0 -> {
                                            previousTabIndex = leftMostVisibleItemIndex
                                            currentTabIndex = rightMostVisibleItemIndex
                                            triggerTabNamesAutoScroll = true
                                            scrollingDirection = -1f
                                            tabScrollState.animateScrollToItem(rightMostVisibleItemIndex)
                                        }

                                        // Moving left.
                                        swipeOffset < 0 -> {
                                            currentTabIndex = leftMostVisibleItemIndex
                                            previousTabIndex = rightMostVisibleItemIndex
                                            triggerTabNamesAutoScroll = true
                                            scrollingDirection = 1f
                                            tabScrollState.animateScrollToItem(leftMostVisibleItemIndex)
                                        }
                                    }
                                } else {
                                    // Reset scroll position.
                                    when {
                                        swipeOffset > 0 -> {
                                            tabScrollState.animateScrollToItem(leftMostVisibleItemIndex)
                                            currentTabIndex = leftMostVisibleItemIndex
                                        }

                                        swipeOffset < 0 -> {
                                            tabScrollState.animateScrollToItem(rightMostVisibleItemIndex)
                                            currentTabIndex = rightMostVisibleItemIndex
                                        }
                                    }
                                }
                                swipeOffset = 0f
                                return available
                            }
                        },
                    ),
            ) {
                /** Total width of all the tabs combined. */
                var totalWidthPx by remember { mutableIntStateOf(0) }

                /** Width the indicator should be drawn with at any time. */
                var indicatorWidthDp by remember { mutableStateOf(0.dp) }

                /** Horizontal offset of the indicator while in mid-scroll. */
                var indicatorXOffsetPx by remember { mutableIntStateOf(0) }
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

                val tabClickedCallback = viewModel.rememberSuspendedAction<Int> { clickedIndex ->
                    jumpingToTabIndex = clickedIndex
                    currentTabIndex = clickedIndex
                    tabScrollState.animateScrollToItem(index = clickedIndex)
                    jumpingToTabIndex = -1
                }

                SubcomposeLayout(
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            // Tab name scrolling.
                            tabNamesScrollOffset += dragAmount.x
                            tabNamesScrollOffset =
                                tabNamesScrollOffset.clampScrollLimits(maxScroll = maxTabNamesScrollLimit)
                        }
                    },
                ) { constraints ->
                    screenWidthPx = maxWidth.toPx()
                    var maxTabHeightDp = 0.dp

                    // Draw tab names.
                    val tabComposedTexts = DEBUGtabNameStrings.mapIndexed { index, name ->
                        subcompose(index) {
                            Text(
                                text = name,
                                modifier = Modifier
                                    .padding(all = 6.dp)
                                    .clickable { tabClickedCallback(index) },
                            )
                        }.single().measure(constraints).also {
                            maxTabHeightDp = maxOf(maxTabHeightDp, it.height.toDp())
                        }
                    }

                    // Draw indicator.
                    val indicator = subcompose(DEBUGtabNameStrings.size + 1) {
                        Box(
                            modifier = Modifier
                                .width(width = indicatorWidthDp)
                                .height(height = INDICATOR_HEIGHT)
                                .background(color = Color.Cyan),
                        )
                    }.single().measure(constraints)
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

                    layout(constraints.maxWidth, constraints.minHeight) {
                        /** A running cumulative sum of rendered tab name widths. */
                        var currentTabNameOffset = 0
                        /** Tab name scroll offset in pixels. */
                        val tabNamesScrollOffsetPx = if (isTabNamesAutoScrolling) {
                            // Adjust manual scroll to include auto-scroll.
                            val rawTabOffset =
                                tabNamesScrollOffset + (scrollingDirection * tabNamesAutoScrollerAnimator.value)
                            rawTabOffset.clampScrollLimits(maxScroll = maxTabNamesScrollLimit).roundToInt()
                        } else {
                            // Manual scroll only.
                            tabNamesScrollOffset.roundToInt()
                        }
                        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                        if (swipeOffset == 0f && visibleItemsInfoState.size == 1) {
                            // Nothing is scrolling, only one tab in view.
                            indicatorWidthDp = tabComposedTexts[currentTabIndex].width.toDp()
                            indicatorXOffsetPx = 0
                        } else {
                            if (!isJumpingToNewTab) {
                                // Determine the in-between position and size for the indicator when scrolling one tab
                                // at a time.
                                leftMostVisibleItem?.let { leftMost ->
                                    // TODO
                                    if (visibleItemsInfoState.size > 1) {
                                        visibleItemsInfoState.find { it.index == leftMost.index + 1 }?.let { nextItem ->
                                            // Determine where the indicator should be and how wide it should be.
                                            val firstVisibleItemWidth = tabComposedTexts[leftMost.index].width
                                            val scrollOffset = -(leftMost.offset.toFloat()) / constraints.maxWidth
                                            val tabSizeDiff =
                                                tabComposedTexts[nextItem.index].width - firstVisibleItemWidth
                                            indicatorWidthDp =
                                                (firstVisibleItemWidth + (tabSizeDiff * scrollOffset)).toDp()
                                            indicatorXOffsetPx = (firstVisibleItemWidth * scrollOffset).toInt()
                                        }
                                    } else {
                                        indicatorXOffsetPx = 0
                                    }
                                }
                            }
                        }
                        tabComposedTexts.forEachIndexed { index, tabText ->
                            // Place tab name.
                            tabText.placeRelative(tabNamesScrollOffsetPx + currentTabNameOffset, 0)
                            if (isJumpingToNewTab) {
                                if (index == jumpingToTabIndex) {
                                    // When jumping to a tab, ignore special positions and sizes for the indicator,
                                    // just match the destination tab's width.
                                    indicator.placeRelative(
                                        x = tabNamesScrollOffsetPx + currentTabNameOffset,
                                        y = (maxTabHeightDp.toPx() - 2).roundToInt(),
                                    )
                                }
                            } else {
                                leftMostVisibleItem?.let {
                                    if (index == it.index) {
                                        indicator.placeRelative(
                                            x = tabNamesScrollOffsetPx + currentTabNameOffset + indicatorXOffsetPx,
                                            y = (maxTabHeightDp.toPx() - 2).roundToInt(),
                                        )
                                    }

                                    if (triggerTabNamesAutoScroll && index == previousTabIndex) {
                                        val indicatorXPosOnScreen =
                                            tabNamesScrollOffsetPx + currentTabNameOffset + indicatorXOffsetPx
                                        val screenHalf = constraints.maxWidth / 2
                                        if (previousTabIndex < currentTabIndex) {
                                            // Indicator scrolled past the middle of the screen to the right.
                                            if (indicatorXPosOnScreen > screenHalf) {
                                                isTabNamesAutoScrolling = true
                                                tabNamesAutoScrollingValue = tabComposedTexts[previousTabIndex].width
                                            }
                                        } else {
                                            // Indicator scrolled past the middle of the screen to the left.
                                            if (indicatorXPosOnScreen < screenHalf) {
                                                isTabNamesAutoScrolling = true
                                                tabNamesAutoScrollingValue = tabComposedTexts[previousTabIndex].width
                                            }
                                        }
                                        triggerTabNamesAutoScroll = false
                                    }
                                }
                            }
                            currentTabNameOffset += tabText.width
                        }

                        totalWidthPx = currentTabNameOffset
                        maxTabNamesScrollLimit = (-currentTabNameOffset) + maxWidth.toPx()
                        if (leftMostVisibleItem == null) {
                            indicator.placeRelative(x = tabNamesScrollOffsetPx, y = (maxTabHeightDp.toPx() - 2).roundToInt())
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
                                .width(maxWidth)
                                .height(maxHeight),
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
                                .width(maxWidth)
                                .height(maxHeight),
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
                    //DUPLICATE TESTS BELOW

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
