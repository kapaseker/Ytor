package io.kapaseker.ytor.storage

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.kapaseker.ytor.database.Download_task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DownloadTask(
    val id: Long,
    val url: String,
    val title: String?,
    val destination: String,
    val status: TaskStatus,
    val createdTime: Long,
    val completedTime: Long?,
    val errorMessage: String?,
    val progress: Float,
    val eta: String?
)

enum class TaskStatus {
    Pending,
    Downloading,
    Completed,
    Failed,
    Paused;

    val dbValue: String
        get() = when (this) {
            Pending -> "pending"
            Downloading -> "downloading"
            Completed -> "completed"
            Failed -> "failed"
            Paused -> "paused"
        }

    companion object {
        fun fromDbValue(value: String): TaskStatus {
            return when (value) {
                "pending" -> Pending
                "downloading" -> Downloading
                "completed" -> Completed
                "failed" -> Failed
                "paused" -> Paused
                else -> Downloading
            }
        }
    }
}

object TaskStore {
    private val database = Database

    private fun Download_task.toDownloadTask(): DownloadTask {
        return DownloadTask(
            id = id,
            url = url,
            title = title,
            destination = destination,
            status = TaskStatus.fromDbValue(status),
            createdTime = created_time,
            completedTime = completed_time,
            errorMessage = error_message,
            progress = progress.toFloat(),
            eta = eta
        )
    }

    val downloadingTasks: Flow<List<DownloadTask>> = database
        .downloadTaskQueries
        .getDownloadingTasks()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toDownloadTask() } }

    val completedTasks: Flow<List<DownloadTask>> = database
        .downloadTaskQueries
        .getCompletedTasks()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toDownloadTask() } }

    fun createTask(url: String, destination: String): Long {
        val createdTime = System.currentTimeMillis()
        database.downloadTaskQueries.insertTask(
            url = url,
            title = null,
            destination = destination,
            status = TaskStatus.Downloading.dbValue,
            created_time = createdTime,
            progress = 0.0,
            eta = null
        )
        println("Created task with ID: $createdTime")
        // 查询刚刚插入的任务以获取ID
        return try {
            val task = database.downloadTaskQueries.getLastInsertedTask(
                url = url,
                created_time = createdTime
            ).executeAsOne()
            task.id
        } catch (e: Exception) {
            // 如果查询失败，返回0（不应该发生）
            0L
        }
    }

    fun updateTaskStatus(id: Long, status: TaskStatus, errorMessage: String? = null) {
        val completedTime = if (status == TaskStatus.Completed || status == TaskStatus.Failed) {
            System.currentTimeMillis()
        } else {
            null
        }
        database.downloadTaskQueries.updateTaskStatus(
            status = status.dbValue,
            completed_time = completedTime,
            error_message = errorMessage,
            id = id
        )
    }

    fun updateTaskTitle(id: Long, title: String) {
        database.downloadTaskQueries.updateTaskTitle(
            title = title,
            id = id
        )
    }

    fun updateTaskProgress(id: Long, progress: Float) {
        // Only update progress if it's greater than or equal to the current progress
        // to handle cases where ytdlp reports non-monotonic progress
        val currentTask = try {
            database.downloadTaskQueries.getTaskById(id).executeAsOneOrNull()
        } catch (e: Exception) {
            null
        }
        
        val currentProgress = currentTask?.progress?.toFloat() ?: 0f
        if (progress >= currentProgress) {
            database.downloadTaskQueries.updateTaskProgress(
                progress = progress.toDouble(),
                id = id
            )
        }
    }

    fun updateTaskDestination(id: Long, destination: String) {
        database.downloadTaskQueries.updateTaskDestination(
            destination = destination,
            id = id
        )
    }

    fun updateTaskEta(id: Long, eta: String?) {
        database.downloadTaskQueries.updateTaskEta(
            eta = eta,
            id = id
        )
    }
}

