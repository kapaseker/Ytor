package io.kapaseker.ytor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.kapaseker.ytor.resource.inPainter
import io.kapaseker.ytor.resource.inString
import io.kapaseker.ytor.theme.AppTheme
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.app_name
import ytor.composeapp.generated.resources.ytor

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        undecorated = false,
        icon = Res.drawable.ytor.inPainter(),
        title = Res.string.app_name.inString(),
    ) {
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                App()
            }
        }
    }
}