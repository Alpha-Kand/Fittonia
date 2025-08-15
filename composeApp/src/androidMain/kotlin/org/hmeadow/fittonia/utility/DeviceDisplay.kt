package org.hmeadow.fittonia.utility

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
import android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.COMPLEX_UNIT_IN
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.MainActivity

@Composable
fun isLandscape() = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE

@Composable
fun isXLARGE(): Boolean {
    return (LocalConfiguration.current.screenLayout and SCREENLAYOUT_SIZE_MASK) == SCREENLAYOUT_SIZE_XLARGE
}

fun MainActivity.inchesToDp(inches: Float): Dp {
    val dimension = TypedValue.applyDimension(
        COMPLEX_UNIT_IN, // Unit.
        inches, // Value.
        resources.displayMetrics, // Metrics.
    )
    return (dimension / resources.displayMetrics.density).dp
}

fun MainActivity.inchesToPx(inches: Float): Float {
    return TypedValue.applyDimension(
        COMPLEX_UNIT_IN, // Unit.
        inches, // Value.
        resources.displayMetrics, // Metrics.
    )
}

fun MainActivity.dpToFloat(dp: Dp): Float {
    return TypedValue.applyDimension(
        COMPLEX_UNIT_DIP, // Unit.
        dp.value, // Value.
        resources.displayMetrics, // Metrics.
    )
}
