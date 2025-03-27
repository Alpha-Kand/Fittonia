package org.hmeadow.fittonia.screens.debugScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.components.ReadOnlyEntries
import org.hmeadow.fittonia.compose.architecture.FittoniaSpacerHeight
import org.hmeadow.fittonia.compose.components.FittoniaButton
import org.hmeadow.fittonia.design.fonts.headingLStyle
import org.hmeadow.fittonia.design.fonts.headingSStyle

@Composable
fun DebugScreenEncryptionTestTab(
    // Private/Public Key
    maxEncryptionBytesPuPr: Int,
    encodedPuPr: ByteArray,
    decodedPuPr: String,
    onEncryptMessagePuPr: () -> Unit,
    onDecryptMessagePuPr: () -> Unit,
    // AES Encryption
    maxEncryptionBytesAES: Int,
    encodedAES: ByteArray,
    decodedAES: String,
    onEncryptMessageAES: () -> Unit,
    onDecryptMessageAES: () -> Unit,
    footerHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Encryption Test",
            style = headingLStyle,
        )

        FittoniaSpacerHeight(height = 35)

        PublicPrivateKeyEncryptionDemo(
            maxEncryptionBytesPuPr = maxEncryptionBytesPuPr,
            encodedPuPr = encodedPuPr,
            decodedPuPr = decodedPuPr,
            onEncryptMessagePuPr = onEncryptMessagePuPr,
            onDecryptMessagePuPr = onDecryptMessagePuPr,
        )

        FittoniaSpacerHeight(height = 35)

        AESKeyEncryptionDemo(
            maxEncryptionBytesAES = maxEncryptionBytesAES,
            encodedAES = encodedAES,
            decodedAES = decodedAES,
            onEncryptMessageAES = onEncryptMessageAES,
            onDecryptMessageAES = onDecryptMessageAES
        )

        FittoniaSpacerHeight(footerHeight)
    }
}

@Composable
private fun PublicPrivateKeyEncryptionDemo(
    maxEncryptionBytesPuPr: Int,
    encodedPuPr: ByteArray,
    decodedPuPr: String,
    onEncryptMessagePuPr: () -> Unit,
    onDecryptMessagePuPr: () -> Unit,
) {
    Column {
        Text(
            text = "Public/Private Key",
            style = headingSStyle,
        )
        FittoniaButton(onClick = onEncryptMessagePuPr) {
            ButtonText(text = "Encrypt -> Original size: $maxEncryptionBytesPuPr")
        }

        FittoniaSpacerHeight(height = 10)

        ReadOnlyEntries(
            entries =
                listOf(
                    "Encoded Size: ${encodedPuPr.size} -> ${encodedPuPr.toList()}",
                    encodedPuPr.decodeToString().let { "Decrypted? -> Size: ${it.length} -> $it" },
                ),
        )

        FittoniaSpacerHeight(height = 10)

        FittoniaButton(onClick = onDecryptMessagePuPr) {
            ButtonText(text = "Decrypt")
        }

        FittoniaSpacerHeight(height = 10)

        ReadOnlyEntries(entries = listOf("Size: ${decodedPuPr.length} -> $decodedPuPr"))
    }
}

@Composable
private fun AESKeyEncryptionDemo(
    maxEncryptionBytesAES: Int,
    encodedAES: ByteArray,
    decodedAES: String,
    onEncryptMessageAES: () -> Unit,
    onDecryptMessageAES: () -> Unit,
) {
    Column {
        Text(
            text = "AES Encryption",
            style = headingSStyle,
        )
        FittoniaButton(onClick = onEncryptMessageAES) {
            ButtonText(text = "Encrypt -> Original size: $maxEncryptionBytesAES")
        }

        FittoniaSpacerHeight(height = 10)

        ReadOnlyEntries(
            entries =
                listOf(
                    "Encoded Size: ${encodedAES.size} -> ${encodedAES.toList()}",
                    encodedAES.decodeToString().let { "Decrypted? -> Size: ${it.length} -> $it" },
                ),
        )

        FittoniaSpacerHeight(height = 10)

        FittoniaButton(onClick = onDecryptMessageAES) {
            ButtonText(text = "Decrypt")
        }

        FittoniaSpacerHeight(height = 10)

        ReadOnlyEntries(entries = listOf("Size: ${decodedAES.length} -> $decodedAES"))
    }
}
