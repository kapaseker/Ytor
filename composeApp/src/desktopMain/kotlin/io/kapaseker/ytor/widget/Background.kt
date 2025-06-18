package io.kapaseker.ytor.widget

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

fun Modifier.drawBackground(
    color: Color
): Modifier = drawBehind {
    drawRect(color)
}

fun Modifier.drawBackground(
    brush: Brush,
): Modifier = drawBehind {
    drawRect(brush)
}

fun Modifier.roundedBorder(
    width: Dp,
    brush: Brush,
    corner: Dp,
): Modifier = border(width = width, brush = brush, shape = RoundedCornerShape(corner))

fun Modifier.roundedBorder(
    width: Dp,
    color: Color,
    corner: Dp,
): Modifier = border(width = width, color = color, shape = RoundedCornerShape(corner))