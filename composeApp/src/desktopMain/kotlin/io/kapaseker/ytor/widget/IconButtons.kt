package io.kapaseker.ytor.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import io.kapaseker.ytor.LocalController
import io.kapaseker.ytor.resource.IconButtonPadding
import io.kapaseker.ytor.resource.IconButtonSize
import io.kapaseker.ytor.resource.inPainter
import io.kapaseker.ytor.resource.inString
import io.kapaseker.ytor.util.R
import org.jetbrains.compose.resources.DrawableResource
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.allDrawableResources
import ytor.composeapp.generated.resources.back

@Composable
fun AppFilledIconButton(
    modifier: Modifier,
    icon: DrawableResource,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick, modifier = modifier.size(IconButtonSize)
    ) {
        Icon(
            painter = icon.inPainter(),
            modifier = Modifier.padding(IconButtonPadding).fillMaxSize(),
            contentDescription = contentDescription
        )
    }
}

@Composable
fun AppIconButton(
    modifier: Modifier,
    icon: DrawableResource,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick, modifier = modifier.size(IconButtonSize)
    ) {
        Icon(
            painter = icon.inPainter(),
            modifier = Modifier.padding(IconButtonPadding).fillMaxSize(),
            contentDescription = contentDescription
        )
    }
}

@Composable
fun BackButton(modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    val currentBack by rememberUpdatedState(onBack)
    val controller = LocalController.current
    AppIconButton(
        modifier = modifier,
        icon = Res.drawable.back,
        contentDescription = Res.string.back.inString()
    ) {
        currentBack?.invoke() ?: controller.navigateUp()
    }
}