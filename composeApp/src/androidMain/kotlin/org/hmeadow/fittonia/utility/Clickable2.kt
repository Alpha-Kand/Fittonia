package org.hmeadow.fittonia.utility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.hmeadow.fittonia.mainActivity.LocalFocusRequester
import org.hmeadow.fittonia.utility.InfoBorderState.clearInfoBorderState

/**
 * A version of 'clickable' that tries to hide any open keyboard if [enabled] is false.
 */
fun Modifier.clickable2(
    enabled: Boolean = true,
    hideKeyboardAlways: Boolean = false,
    onClick: () -> Unit,
): Modifier = composed {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = LocalFocusRequester.current
    val hideKeyboard = {
        keyboard?.hide()
        focusRequester.requestFocus()
        clearInfoBorderState()
    }
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple().takeIf { enabled },
        onClick = {
            if (enabled) {
                onClick()
                if (hideKeyboardAlways) {
                    hideKeyboard()
                }
            } else {
                hideKeyboard()
            }
        },
    )
}
