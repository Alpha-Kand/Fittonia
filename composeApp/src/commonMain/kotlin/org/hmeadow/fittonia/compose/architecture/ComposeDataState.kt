package org.hmeadow.fittonia.compose.architecture

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import org.hmeadow.fittonia.compose.components.FittoniaLoadingIndicator

sealed interface ComposeDataState<out T> {
    class Loading<T> : ComposeDataState<T>
    data class Success<T>(val value: T) : ComposeDataState<T>
    class Failure<T> : ComposeDataState<T>
}

val <T> Flow<T>.dataState: ComposeDataState<T>
    @Composable
    get() {
        val flow = this
        return produceState<ComposeDataState<T>>(ComposeDataState.Loading(), flow) {
            try {
                flow.collect { value = ComposeDataState.Success(it) }
            } catch (e: Throwable) {
                value = ComposeDataState.Failure()
            }
        }.value
    }

@Composable
fun <T> LoadingCompose(
    composeDataState: ComposeDataState<T>,
    modifier: Modifier = Modifier,
    loadingBlock: @Composable () -> Unit = { FittoniaLoadingIndicator() },
    failureBlock: @Composable () -> Unit = {},
    successBlock: @Composable (T) -> Unit,
) {
    Box(modifier = modifier) {
        when (composeDataState) {
            is ComposeDataState.Loading -> loadingBlock()
            is ComposeDataState.Failure -> failureBlock()
            is ComposeDataState.Success -> successBlock(composeDataState.value)
        }
    }
}
