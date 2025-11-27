package org.hmeadow.fittonia.utility

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.design.fonts.emoticonStyle

class ContinueFlow<T>(flow: Flow<T>, block: suspend (T) -> ContinueFlag) {
    private var errorFlag = false
    private val continueStatusState: Flow<ContinueStatus> = flow.map {
        block(it).let { continueFlag ->
            lostFocusState.update { false }
            if (continueFlag != ContinueFlag.Fail) {
                errorFlag = true
                ContinueStatus.Success
            } else {
                if (errorFlag) {
                    ContinueStatus.Error
                } else {
                    ContinueStatus.Standby
                }
            }
        }
    }
    private val lostFocusState = MutableStateFlow(false)
    val result: Flow<ContinueStatus> = combine(
        lostFocusState,
        continueStatusState,
    ) { lostFocus, continueStatus ->
        if (lostFocus && continueStatus != ContinueStatus.Success) {
            ContinueStatus.Error
        } else {
            continueStatus
        }
    }

    fun focusChanged(focusEvent: FocusState) {
        if (!focusEvent.isFocused) {
            lostFocusState.update { true }
        }
    }

    @Composable
    fun collect(): ContinueStatus = result.collectAsState(initial = ContinueStatus.Standby).value

    enum class ContinueStatus {
        Standby,
        Success,
        Error,
    }

    enum class ContinueFlag {
        Pass,
        Fail,
    }
}

val ContinueFlow.ContinueStatus.canContinue: Boolean
    get() = this == ContinueFlow.ContinueStatus.Success

@Composable
fun ContinueStatusIcon(modifier: Modifier = Modifier, continueStatus: ContinueFlow.ContinueStatus) {
    Text(
        modifier = modifier,
        text = when (continueStatus) {
            ContinueFlow.ContinueStatus.Success -> stringResource(R.string.success_emoticon)
            ContinueFlow.ContinueStatus.Error -> stringResource(R.string.failure_emoticon)
            else -> stringResource(R.string.right_arrow_emoticon)
        },
        style = emoticonStyle,
    )
}
