package io.kapaseker.ytor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.kapaseker.ytor.theme.AppTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        undecorated = false,
        title = "Ytor",
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