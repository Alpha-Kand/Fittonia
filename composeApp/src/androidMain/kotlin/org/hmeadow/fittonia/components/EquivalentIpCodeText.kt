package org.hmeadow.fittonia.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.hmeadow.fittonia.R
import org.hmeadow.fittonia.design.fonts.psstStyle
import org.hmeadow.fittonia.utility.decodeIpAddress
import org.hmeadow.fittonia.utility.encodeIpAddress
import org.hmeadow.fittonia.utility.tryOrNull

sealed interface EquivalentIPCode {
    data object Neither : EquivalentIPCode
    data class MatchingIP(val ip: String) : EquivalentIPCode
    data class MatchingCode(val code: String) : EquivalentIPCode
}

@Composable
fun EquivalentIpCodeText(equivalentIPCode: EquivalentIPCode) {
    when (equivalentIPCode) {
        is EquivalentIPCode.MatchingIP -> stringResource(R.string.encode_decode_matching_ip, equivalentIPCode.ip)
        is EquivalentIPCode.MatchingCode -> stringResource(R.string.encode_decode_matching_code, equivalentIPCode.code)
        is EquivalentIPCode.Neither -> null
    }?.let { text ->
        Text(
            text = text,
            style = psstStyle,
            color = Color(color = 0x99000000),
        )
    }
}

/**
 * Converts a string IP address or IP code to an [EquivalentIPCode].
 */
fun decipherIpAndCode(ip: String): EquivalentIPCode {
    return tryOrNull {
        EquivalentIPCode.MatchingCode(code = encodeIpAddress(ipAddress = ip))
    } ?: tryOrNull {
        EquivalentIPCode.MatchingIP(ip = decodeIpAddress(ipAddress = ip))
    } ?: EquivalentIPCode.Neither
}
