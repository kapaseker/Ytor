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

val FloatingActionButtonIconSize = 24.dp
val CardVerticalPadding = 4.dp
val CardElevation = 2.dp
val SpacingMedium = 8.dp
val SpacingSmall = 4.dp
val IconButtonTopPadding = 8.dp
val IconButtonEndPadding = 4.dp

enum class ButtonSize(val size: Dp, val padding: Dp) {
    Small(IconButtonSize, IconButtonPadding),
    XSmall(SmallIconButtonSize, SmallIconButtonPadding)
}