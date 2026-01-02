package io.kapaseker.ytor.storage

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
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
    val progress: Float
)

enum class TaskStatus {
    Downloading,
    Completed,
    Failed,
    Paused;

    val dbValue: String
        get() = when (this) {
            Downloading -> "downloading"
            Completed -> "completed"
            Failed -> "failed"
            Paused -> "paused"
        }

    companion object {
        fun fromDbValue(value: String): TaskStatus {
            return when (value) {
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

    val downloadingTasks: Flow<List<DownloadTask>> = database
        .downloadTaskQueries
        .getDownloadingTasks()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows ->
            rows.map { row ->
                DownloadTask(
                    id = row.id,
                    url = row.url,
                    title = row.title,
                    destination = row.destination,
                    status = TaskStatus.fromDbValue(row.status),
                    createdTime = row.created_time,
                    completedTime = row.completed_time,
                    errorMessage = row.error_message,
                    progress = row.progress.toFloat()
                )
            }
        }

    val completedTasks: Flow<List<DownloadTask>> = database
        .downloadTaskQueries
        .getCompletedTasks()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows ->
            rows.map { row ->
                DownloadTask(
                    id = row.id,
                    url = row.url,
                    title = row.title,
                    destination = row.destination,
                    status = TaskStatus.fromDbValue(row.status),
                    createdTime = row.created_time,
                    completedTime = row.completed_time,
                    errorMessage = row.error_message,
                    progress = row.progress.toFloat()
                )
            }
        }

    fun createTask(url: String, destination: String): Long {
        val createdTime = System.currentTimeMillis()
        database.downloadTaskQueries.insertTask(
            url = url,
            title = null,
            destination = destination,
            status = TaskStatus.Downloading.dbValue,
            created_time = createdTime,
            progress = 0.0
        )
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
        database.downloadTaskQueries.updateTaskProgress(
            progress = progress.toDouble(),
            id = id
        )
    }
}

