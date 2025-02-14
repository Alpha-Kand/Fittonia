package org.hmeadow.fittonia.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import org.hmeadow.fittonia.compose.components.FittoniaButtonScope

@Composable
fun FittoniaButtonScope.ButtonIcon(@DrawableRes drawableRes:Int) {
    ButtonIcon(painterResource(drawableRes))
}
