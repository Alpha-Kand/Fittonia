package org.hmeadow.fittonia.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Locale

@Composable
fun rememberPercentageFormat(
    percentage: Double,
    minFraction: Int? = null,
    maxFraction: Int? = 2,
): String = remember(
    percentage,
    minFraction,
    maxFraction,
) {
    NumberFormat.getPercentInstance(Locale.getDefault()).apply {
        maxFraction?.let { maximumFractionDigits = maxFraction }
        minFraction?.let { minimumFractionDigits = minFraction }
    }.format(percentage)
}
