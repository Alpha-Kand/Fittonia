package org.hmeadow.fittonia.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.compose.components.FittoniaButtonScope
import org.hmeadow.fittonia.compose.components.FittoniaCircleButtonConstants.CIRCLE_BUTTON_ICON_SIZE

@Composable
fun FittoniaButtonScope.ButtonIcon(@DrawableRes drawableRes: Int, size: Dp? = null) {
    ButtonIcon(painter = painterResource(drawableRes), size = size)
}

@Composable
fun FittoniaButtonScope.CircleButtonIcon(@DrawableRes drawableRes: Int) {
    ButtonIcon(painter = painterResource(drawableRes), size = CIRCLE_BUTTON_ICON_SIZE.dp)
}
