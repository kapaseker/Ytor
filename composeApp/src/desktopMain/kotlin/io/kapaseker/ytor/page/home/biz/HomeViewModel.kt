package io.kapaseker.ytor.page.home.biz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.kapaseker.ytor.storage.Store
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 下载状态
 */
data class DownloadState(
    val status: DownloadStatus = DownloadStatus.Idle,
    val progress: Float = 0f,           // 0-100 百分比
    val speed: String = "",             // 下载速度，如 "5.00MiB/s"
    val eta: String = "",               // 预计剩余时间，如 "01:40"
    val totalSize: String = "",         // 总大小，如 "100.00MiB"
    val downloadedSize: String = "",    // 已下载大小，如 "10.50MiB"
    val errorMessage: String? = null    // 错误信息
)

enum class DownloadStatus {
    Idle,           // 空闲
    Preparing,      // 准备中（获取视频信息）
    Downloading,    // 下载中
    Merging,        // 合并中（视频+音频）
    Completed,      // 完成
    Error           // 错误
}

class HomeViewModel : ViewModel() {

    private val _destinationHistory = MutableStateFlow<ImmutableList<String>>(persistentListOf())
    val destinationHistory: StateFlow<ImmutableList<String>> = _destinationHistory.asStateFlow()

    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    // yt-dlp 输出解析正则
    // 匹配: [download]  10.5% of  100.00MiB at    5.00MiB/s ETA 00:18
    private val progressRegex = Regex(
        """\[download\]\s+([\d.]+)%\s+of\s+~?\s*([\d.]+\s*\w+)\s+at\s+([\d.]+\s*\w+/s|Unknown speed)\s+ETA\s+(\S+)"""
    )

    // 匹配: [download]  10.5% of  100.00MiB at    5.00MiB/s ETA 00:18 (frag 5/100)
    private val fragProgressRegex = Regex(
        """\[download\]\s+([\d.]+)%\s+of\s+~?\s*([\d.]+\s*\w+)\s+at\s+([\d.]+\s*\w+/s|Unknown speed)\s+ETA\s+(\S+)\s+\(frag\s+\d+/\d+\)"""
    )

    // 匹配下载完成: [download] 100% of 100.00MiB in 00:10:05
    private val completedRegex = Regex(
        """\[download\]\s+100%\s+of\s+([\d.]+\s*\w+)\s+in\s+(\S+)"""
    )

    // 匹配合并: [Merger] Merging formats into ...
    private val mergerRegex = Regex("""\[Merger\]""")

    // 匹配已下载: [download] ... has already been downloaded
    private val alreadyDownloadedRegex = Regex("""\[download\].*has already been downloaded""")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            Store.destinationHistory.collectLatest { history ->
                _destinationHistory.update {
                    history.items.map { it.path }.toImmutableList()
                }
            }
        }
    }

    fun deleteHistory(item: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Store.remove(item)
        }
    }

    fun download(input: String, dir: String) {
        println("download input: $input")

        val saveOption = dir.takeIf { it.isNotEmpty() } ?: "./"

        viewModelScope.launch(Dispatchers.IO) {
            // 重置状态
            _downloadState.update {
                DownloadState(status = DownloadStatus.Preparing)
            }

            Store.addDestination(dir)

            runCatching {
                val process = Runtime.getRuntime().exec(
                    arrayOf(
                        "yt-dlp",
                        "-P", saveOption,
                        "-f", "bestvideo[height<=1080][ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
                        "--newline",  // 每行输出进度，便于解析
                        "--progress",
                        input
                    )
                )

                // 读取标准输出
                process.inputStream.bufferedReader().forEachLine { line ->
                    println(line)
                    parseYtDlpOutput(line)
                }

                // 读取错误输出
                val errorOutput = process.errorStream.bufferedReader().readText()
                if (errorOutput.isNotBlank()) {
                    println("Error: $errorOutput")
                }

                process.waitFor()
            }.onSuccess { exitCode ->
                if (exitCode == 0) {
                    _downloadState.update {
                        it.copy(
                            status = DownloadStatus.Completed,
                            progress = 100f
                        )
                    }
                    println("done")
                } else {
                    _downloadState.update {
                        it.copy(
                            status = DownloadStatus.Error,
                            errorMessage = "yt-dlp exited with code $exitCode"
                        )
                    }
                }
            }.onFailure { e ->
                _downloadState.update {
                    it.copy(
                        status = DownloadStatus.Error,
                        errorMessage = e.message
                    )
                }
                e.printStackTrace()
            }
        }
    }

    private fun parseYtDlpOutput(line: String) {
        when {
            // 检查是否正在合并
            mergerRegex.containsMatchIn(line) -> {
                _downloadState.update {
                    it.copy(
                        status = DownloadStatus.Merging,
                        progress = 100f,
                        speed = "",
                        eta = "合并中..."
                    )
                }
            }

            // 检查是否已下载
            alreadyDownloadedRegex.containsMatchIn(line) -> {
                _downloadState.update {
                    it.copy(
                        status = DownloadStatus.Completed,
                        progress = 100f
                    )
                }
            }

            // 检查下载完成
            completedRegex.containsMatchIn(line) -> {
                completedRegex.find(line)?.destructured?.let { (totalSize, _) ->
                    _downloadState.update {
                        it.copy(
                            status = DownloadStatus.Downloading,
                            progress = 100f,
                            totalSize = totalSize.trim(),
                            speed = "",
                            eta = ""
                        )
                    }
                }
            }

            // 检查下载进度（带分片信息）
            fragProgressRegex.containsMatchIn(line) -> {
                fragProgressRegex.find(line)?.let { updateProgressFromMatch(it) }
            }

            // 检查下载进度（普通）
            progressRegex.containsMatchIn(line) -> {
                progressRegex.find(line)?.let { updateProgressFromMatch(it) }
            }
        }
    }

    private fun updateProgressFromMatch(match: MatchResult) {
        val (percent, totalSize, speed, eta) = match.destructured
        val progress = percent.toFloatOrNull() ?: 0f

        _downloadState.update {
            it.copy(
                status = DownloadStatus.Downloading,
                progress = progress,
                totalSize = totalSize.trim(),
                speed = speed.trim(),
                eta = eta.trim()
            )
        }
    }

    /**
     * 取消下载（可选实现）
     */
    fun cancelDownload() {
        // 如果需要取消功能，需要保存 Process 引用并调用 destroy()
        _downloadState.update {
            DownloadState(status = DownloadStatus.Idle)
        }
    }
}