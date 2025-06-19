package io.kapaseker.ytor.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import io.kapaseker.ytor.LocalController
import io.kapaseker.ytor.resource.IconButtonPadding
import io.kapaseker.ytor.resource.IconButtonSize
import io.kapaseker.ytor.resource.SmallIconButtonPadding
import io.kapaseker.ytor.resource.SmallIconButtonSize
import io.kapaseker.ytor.resource.inPainter
import io.kapaseker.ytor.resource.inString
import org.jetbrains.compose.resources.DrawableResource
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.back

@Composable
fun AppRoundFilledIconButton(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(IconButtonSize).semantics {
            contentDescription?.let {
                this.contentDescription = it
            }
            this.role = Role.Button
        },
    ) {
        Icon(
            painter = icon.inPainter(),
            modifier = Modifier.padding(IconButtonPadding).fillMaxSize(),
            contentDescription = null,
        )
    }
}

@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(IconButtonSize).semantics {
            contentDescription?.let {
                this.contentDescription = it
            }
            this.role = Role.Button
        }
    ) {
        Icon(
            painter = icon.inPainter(),
            modifier = Modifier.padding(IconButtonPadding).fillMaxSize(),
            contentDescription = contentDescription
        )
    }
}


@Composable
fun AppToggleIconButtonSmall(
    modifier: Modifier = Modifier,
    checked: Boolean,
    icon: DrawableResource,
    contentDescription: String?,
    onCheckedChange: (Boolean) -> Unit,
) {
    IconToggleButton(
        modifier = modifier.size(SmallIconButtonSize).semantics {
            contentDescription?.let {
                this.contentDescription = it
            }
            this.role = Role.Checkbox
        },
        checked = checked,
        onCheckedChange = onCheckedChange
    ) {
        Icon(
            painter = icon.inPainter(),
            modifier = Modifier.padding(SmallIconButtonPadding).fillMaxSize(),
            contentDescription = contentDescription
        )
    }
}

@Composable
fun AppToggleIconButton(
    modifier: Modifier = Modifier,
    checked: Boolean,
    icon: DrawableResource,
    contentDescription: String?,
    onCheckedChange: (Boolean) -> Unit,
) {
    IconToggleButton(
        modifier = modifier.size(IconButtonSize).semantics {
            contentDescription?.let {
                this.contentDescription = it
            }
            this.role = Role.Checkbox
        },
        checked = checked,
        onCheckedChange = onCheckedChange
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