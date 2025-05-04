package org.hmeadow.fittonia.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hmeadow.fittonia.design.fonts.paragraphTextStyle
import org.hmeadow.fittonia.screens.overviewScreen.measureTextWidth

@Composable
fun FittoniaLinkText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Text(
            text = text,
            color = Color.Blue,
            style = paragraphTextStyle,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .width(measureTextWidth(text = text, style = paragraphTextStyle))
                .height(1.dp)
                .background(Color.Blue),
        )
    }
}
