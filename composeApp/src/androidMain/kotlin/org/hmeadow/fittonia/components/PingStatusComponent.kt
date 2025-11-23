package org.hmeadow.fittonia.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerWidth
import org.hmeadow.fittonia.compose.components.FittoniaLoadingIndicator
import org.hmeadow.fittonia.design.Spacing.spacing16
import org.hmeadow.fittonia.design.Spacing.spacing8
import org.hmeadow.fittonia.design.fonts.emoticonStyle
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.models.PingStatus

@Composable
fun PingStatusComponent(pingStatus: PingStatus) {
    when (pingStatus) {
        is PingStatus.NoPing -> Unit
        is PingStatus.Processing -> Row {
            Text(
                text = stringResource(R.string.connection_emoticon),
                style = emoticonStyle,
            )
            FittoniaSpacerWidth(width = spacing16)
            Text(
                text = stringResource(R.string.send_files_screen_ping_processing),
                style = paragraphTextStyle,
            )
            FittoniaSpacerWidth(width = spacing8)
            FittoniaLoadingIndicator()
        }

        is PingStatus.Success -> Row {
            Text(
                text = stringResource(R.string.success_emoticon),
                style = emoticonStyle,
            )
            FittoniaSpacerWidth(width = spacing16)
            Text(
                text = stringResource(R.string.send_files_screen_ping_success),
                style = paragraphTextStyle,
            )
        }

        is PingStatus.Failure -> Row {
            Text(
                text = stringResource(R.string.failure_emoticon),
                style = emoticonStyle,
            )
            FittoniaSpacerWidth(width = spacing16)
            Text(
                text = stringResource(R.string.send_files_screen_ping_failure),
                style = paragraphTextStyle,
            )
        }
    }
}
