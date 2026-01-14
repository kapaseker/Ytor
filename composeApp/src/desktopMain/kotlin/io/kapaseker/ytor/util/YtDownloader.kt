package io.kapaseker.ytor.util

import io.kapaseker.ytor.storage.Store
import io.kapaseker.ytor.storage.TaskStore
import io.kapaseker.ytor.storage.TaskStatus
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

/**
 * YouTube 下载器单例
 * 负责管理所有下载任务和状态更新
 */
object YtDownloader {
    private val downloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _downloadState = MutableStateFlow<DownloadState?>(null)
    val downloadStateFlow: Flow<DownloadState?> = _downloadState.asStateFlow()

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

    /**
     * Converts video title to a valid folder name.
     * - Removes illegal characters for file names
     * - Replaces spaces with underscores
     * - Adds leading underscore
     */
    private fun titleToFolderName(title: String): String {
        // Remove illegal characters for file names (Windows: /\:*?"<>|)
        val sanitized = title.replace(Regex("[/\\\\:*?\"<>|]"), "")
        // Replace spaces with underscores and add leading underscore
        return "_" + sanitized.replace(" ", "_")
    }

    /**
     * 开始下载任务
     */
    fun download(input: String, dir: String) {
        println("download input: $input")

        val baseDir = dir.takeIf { it.isNotEmpty() } ?: "./"

        downloadScope.launch {
            // 重置状态
            _downloadState.value = DownloadState(status = DownloadStatus.Preparing)

            Store.addDestination(dir)

            var taskId: Long = 0L

            runCatching {
                // Step 1: 使用 --print 获取视频标题
                var videoTitle: String? = null
                runCatching {
                    val titleProcess = Runtime.getRuntime().exec(
                        arrayOf(
                            "yt-dlp",
                            "--cookies-from-browser", "firefox",  // 从浏览器获取 cookies
                            "--print", "%(title)s",
                            input
                        )
                    )
                    videoTitle = titleProcess.inputStream.bufferedReader().readLine()?.takeIf { it.isNotBlank() }
                    titleProcess.waitFor()
                }.onFailure { e ->
                    println("Failed to get video title: ${e.message}")
                }

                // Step 2: Generate folder name from title or use timestamp fallback
                val folderName = videoTitle?.let { titleToFolderName(it) }
                    ?: "_download_${System.currentTimeMillis()}"

                // Step 3: Create the video-specific subfolder
                val downloadDir = File(baseDir, folderName)
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }
                val actualDownloadPath = downloadDir.absolutePath

                // Step 4: 创建任务记录 with the actual download folder path
                taskId = TaskStore.createTask(url = input, destination = actualDownloadPath)

                // Step 5: 如果有标题，更新任务标题
                videoTitle?.let { title ->
                    TaskStore.updateTaskTitle(taskId, title)
                }

                // Step 6: 执行下载 to the video-specific folder
                val process = Runtime.getRuntime().exec(
                    arrayOf(
                        "yt-dlp",
                        "-P", actualDownloadPath,
                        "-f", "best[height<=1080][ext=mp4]/best[ext=mp4]/best",
                        "--cookies-from-browser", "firefox",  // 从浏览器获取 cookies
                        "--newline",  // 每行输出进度，便于解析
                        "--progress",
                        input
                    )
                )

                // 读取标准输出和错误输出（需要并行读取，避免阻塞）
                val errorLines = mutableListOf<String>()
                
                // 启动错误输出读取线程
                val errorReader = Thread {
                    process.errorStream.bufferedReader().forEachLine { line ->
                        errorLines.add(line)
                        println("Error: $line")
                        // 检查是否是关键错误
                        if (line.contains("ERROR", ignoreCase = true) || 
                            line.contains("Sign in to confirm", ignoreCase = true)) {
                            parseErrorOutput(line, taskId)
                        }
                    }
                }
                errorReader.start()

                // 读取标准输出
                process.inputStream.bufferedReader().forEachLine { line ->
                    println(line)
                    parseYtDlpOutput(line, taskId)
                }

                // 等待错误输出读取完成
                errorReader.join()

                process.waitFor()
            }.onSuccess { exitCode ->
                if (exitCode == 0) {
                    _downloadState.value = _downloadState.value?.copy(
                        status = DownloadStatus.Completed,
                        progress = 100f
                    )
                    // 更新任务状态为已完成，并清空ETA
                    if (taskId > 0) {
                        TaskStore.updateTaskStatus(taskId, TaskStatus.Completed)
                        TaskStore.updateTaskEta(taskId, null)
                    }
                    println("done")
                } else {
                    val errorMsg = "yt-dlp exited with code $exitCode"
                    _downloadState.value = _downloadState.value?.copy(
                        status = DownloadStatus.Error,
                        errorMessage = errorMsg
                    )
                    // 更新任务状态为失败
                    if (taskId > 0) {
                        TaskStore.updateTaskStatus(taskId, TaskStatus.Failed, errorMsg)
                    }
                }
            }.onFailure { e ->
                val errorMsg = e.message ?: "Unknown error"
                _downloadState.value = _downloadState.value?.copy(
                    status = DownloadStatus.Error,
                    errorMessage = errorMsg
                )
                // 更新任务状态为失败
                if (taskId > 0) {
                    TaskStore.updateTaskStatus(taskId, TaskStatus.Failed, errorMsg)
                }
                e.printStackTrace()
            }
        }
    }

    private fun parseYtDlpOutput(line: String, taskId: Long = 0L) {
        when {
            // 检查是否正在合并
            mergerRegex.containsMatchIn(line) -> {
                _downloadState.value = _downloadState.value?.copy(
                    status = DownloadStatus.Merging,
                    progress = 100f,
                    speed = "",
                    eta = "合并中..."
                )
                // 清空ETA，因为合并阶段不需要显示ETA
                if (taskId > 0) {
                    TaskStore.updateTaskEta(taskId, null)
                }
            }

            // 检查是否已下载
            alreadyDownloadedRegex.containsMatchIn(line) -> {
                _downloadState.value = _downloadState.value?.copy(
                    status = DownloadStatus.Completed,
                    progress = 100f
                )
                // 更新任务状态为已完成，并清空ETA
                if (taskId > 0) {
                    TaskStore.updateTaskStatus(taskId, TaskStatus.Completed)
                    TaskStore.updateTaskEta(taskId, null)
                }
            }

            // 检查下载完成
            completedRegex.containsMatchIn(line) -> {
                completedRegex.find(line)?.destructured?.let { (totalSize, _) ->
                    _downloadState.value = _downloadState.value?.copy(
                        status = DownloadStatus.Downloading,
                        progress = 100f,
                        totalSize = totalSize.trim(),
                        speed = "",
                        eta = ""
                    )
                    // 清空ETA，因为下载已完成
                    if (taskId > 0) {
                        TaskStore.updateTaskEta(taskId, null)
                    }
                }
            }

            // 检查下载进度（带分片信息）
            fragProgressRegex.containsMatchIn(line) -> {
                fragProgressRegex.find(line)?.let { updateProgressFromMatch(it, taskId) }
            }

            // 检查下载进度（普通）
            progressRegex.containsMatchIn(line) -> {
                progressRegex.find(line)?.let { updateProgressFromMatch(it, taskId) }
            }
        }
    }

    private fun updateProgressFromMatch(match: MatchResult, taskId: Long = 0L) {
        val (percent, totalSize, speed, eta) = match.destructured
        val progress = percent.toFloatOrNull() ?: 0f
        val trimmedEta = eta.trim()

        // 容错逻辑：如果 ETA 为 00:00，不更新进度和 ETA
        if (trimmedEta == "00:00" || trimmedEta == "0:00") {
            return
        }

        if ((_downloadState.value?.progress ?: 0f) < progress) {
            _downloadState.update {
                it?.copy(
                    progress = progress,
                    totalSize = totalSize.trim(),
                    speed = speed.trim(),
                    eta = trimmedEta
                ) ?: DownloadState(
                    status = DownloadStatus.Downloading,
                    progress = progress,
                    totalSize = totalSize.trim(),
                    speed = speed.trim(),
                    eta = trimmedEta
                )
            }
        }

        // 更新任务进度和ETA
        if (taskId > 0) {
            TaskStore.updateTaskProgress(taskId, progress)
            TaskStore.updateTaskEta(taskId, if (trimmedEta.isNotEmpty() && trimmedEta.lowercase() != "unknown") trimmedEta else null)
        }
    }

    /**
     * 解析错误输出
     */
    private fun parseErrorOutput(line: String, taskId: Long = 0L) {
        when {
            line.contains("Sign in to confirm", ignoreCase = true) -> {
                val errorMsg = "YouTube 需要验证身份。请确保已登录 Chrome 浏览器，或使用 --cookies 参数提供 cookies。"
                _downloadState.value = _downloadState.value?.copy(
                    status = DownloadStatus.Error,
                    errorMessage = errorMsg
                )
                if (taskId > 0) {
                    TaskStore.updateTaskStatus(taskId, TaskStatus.Failed, errorMsg)
                }
            }
            line.contains("ERROR", ignoreCase = true) -> {
                // 提取错误信息（去除 ERROR: 前缀）
                val errorMsg = line.substringAfter("ERROR:").trim().takeIf { it.isNotBlank() } 
                    ?: "下载失败，请检查网络连接或视频链接"
                _downloadState.value = _downloadState.value?.copy(
                    status = DownloadStatus.Error,
                    errorMessage = errorMsg
                )
                if (taskId > 0) {
                    TaskStore.updateTaskStatus(taskId, TaskStatus.Failed, errorMsg)
                }
            }
        }
    }

    /**
     * 取消下载（可选实现）
     */
    fun cancelDownload() {
        // 如果需要取消功能，需要保存 Process 引用并调用 destroy()
//        _downloadState.value = null
        TODO()
    }
}

