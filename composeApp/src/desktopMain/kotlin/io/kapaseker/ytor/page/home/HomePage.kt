package io.kapaseker.ytor.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import io.kapaseker.ytor.page.home.biz.HomeViewModel
import io.kapaseker.ytor.resource.inPainter
import ytor.composeapp.generated.resources.Res
import ytor.composeapp.generated.resources.compose_multiplatform

@Composable
fun HomePage(
    entry: NavBackStackEntry,
    vm: HomeViewModel = viewModel { HomeViewModel() }
) {
    var showContent by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { showContent = !showContent }) {
            Text("Click me!")
        }
        AnimatedVisibility(showContent) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = Res.drawable.compose_multiplatform.inPainter(), contentDescription = null)
                Text(text = "Hello Compose")
            }
        }
    }
}