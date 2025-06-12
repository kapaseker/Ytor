package io.kapaseker.ytor

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.kapaseker.ytor.theme.AppTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Ytor",
    ) {
        AppTheme {
            App()
        }
    }
}