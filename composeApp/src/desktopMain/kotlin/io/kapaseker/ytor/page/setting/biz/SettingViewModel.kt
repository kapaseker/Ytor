package io.kapaseker.ytor.page.setting.biz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {

    private val _ffmpegVersion = MutableStateFlow("")
    val ffmpegVersion: StateFlow<String> = _ffmpegVersion.asStateFlow()

    private val _ytVersion = MutableStateFlow("")
    val ytVersion: StateFlow<String> = _ytVersion.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val ffmpegProcess = Runtime.getRuntime().exec(arrayOf("ffmpeg","-version"))
            val ffmpegResult = ffmpegProcess.inputStream.bufferedReader().readLine()
            _ffmpegVersion.update { ffmpegResult }

            val ytProcess = Runtime.getRuntime().exec(arrayOf("yt-dlp","--version"))
            val ytResult = ytProcess.inputStream.bufferedReader().readLine()
            _ytVersion.update { ytResult }
        }
    }
}