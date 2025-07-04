package io.kapaseker.ytor.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import io.kapaseker.ytor.LocalController
import io.kapaseker.ytor.resource.ButtonSize
import io.kapaseker.ytor.resource.IconButtonPadding
import io.kapaseker.ytor.resource.IconButtonSize
import io.kapaseker.ytor.resource.SmallIconButtonPadding
import io.kapaseker.ytor.resource.SmallIconButtonSize
import io.kapaseker.ytor.resource.inPainter
import io.kapaseker.ytor.resource.inString
import jdk.jfr.Enabled
import org.jetbrains.compose.resources.DrawableResource
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.back

enum class IconButtonStyle {
    Normal, Filled, Outlined
}

@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    style :IconButtonStyle = IconButtonStyle.Normal,
    size: ButtonSize = ButtonSize.Small,
    contentDescription: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {

    val iconContent: @Composable () -> Unit = {
        Icon(
            painter = icon.inPainter(),
            modifier = Modifier.padding(size.padding).fillMaxSize(),
            contentDescription = null,
        )
    }

    when(style) {
        IconButtonStyle.Normal -> {
            IconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.size(size.size).semantics {
                    contentDescription?.let {
                        this.contentDescription = it
                    }
                    this.role = Role.Button
                },
                content = iconContent,
            )
        }

        IconButtonStyle.Filled -> {
            FilledIconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.size(size.size).semantics {
                    contentDescription?.let {
                        this.contentDescription = it
                    }
                    this.role = Role.Button
                },
                content = iconContent,
            )
        }

        IconButtonStyle.Outlined -> {
            OutlinedIconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.size(size.size).semantics {
                    contentDescription?.let {
                        this.contentDescription = it
                    }
                    this.role = Role.Button
                },
                content = iconContent,
            )
        }
    }
}

@Composable
fun AppToggleIconButton(
    modifier: Modifier = Modifier,
    checked: Boolean,
    icon: DrawableResource,
    size: ButtonSize = ButtonSize.Small,
    contentDescription: String? = null,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    IconToggleButton(
        modifier = modifier.size(size.size).semantics {
            contentDescription?.let {
                this.contentDescription = it
            }
            this.role = Role.Checkbox
        },
        checked = checked,
        enabled = enabled,
        onCheckedChange = onCheckedChange
    ) {
        Icon(
            painter = icon.inPainter(),
            modifier = Modifier.padding(size.padding).fillMaxSize(),
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