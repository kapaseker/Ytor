package io.kapaseker.ytor.resource

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp



val IconButtonSize = 40.dp
val IconButtonPadding = 8.dp

val SmallIconButtonSize = 32.dp
val SmallIconButtonPadding = 6.dp

val PagePadding = 12.dp
val PaddingMedium = 8.dp

val SingleLineListItemHeight = 56.dp
val SingleLineListItemPaddingHorizontal = 16.dp

enum class ButtonSize(val size: Dp, val padding: Dp) {
    Small(IconButtonSize, IconButtonPadding),
    XSmall(SmallIconButtonSize, SmallIconButtonPadding)
}