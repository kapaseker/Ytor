package io.kapaseker.ytor.page.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import io.kapaseker.ytor.page.setting.biz.SettingViewModel
import io.kapaseker.ytor.widget.BackButton

@Composable
fun SettingPage(
    entry: NavBackStackEntry,
    vm: SettingViewModel = viewModel { SettingViewModel() }
) {
    val yt by vm.ytVersion.collectAsState()
    val ffmpeg by vm.ffmpegVersion.collectAsState()

    Box(
        modifier = Modifier.safeContentPadding().fillMaxSize()
    ) {
        BackButton(
            modifier = Modifier.padding(12.dp)
        )

        Column(modifier = Modifier.align(Alignment.Center)) {
            Text("Yt-dlp Version: $yt")
            Text("FFmpeg Version: $ffmpeg")
        }
    }
}