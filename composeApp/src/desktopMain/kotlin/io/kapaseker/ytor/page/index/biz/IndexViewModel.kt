package io.kapaseker.ytor.page.index.biz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.kapaseker.ytor.storage.DownloadTask
import io.kapaseker.ytor.storage.TaskStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class IndexViewModel : ViewModel() {

    private val _downloadingTasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val downloadingTasks: StateFlow<List<DownloadTask>> = _downloadingTasks.asStateFlow()

    private val _completedTasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val completedTasks: StateFlow<List<DownloadTask>> = _completedTasks.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            TaskStore.downloadingTasks
                .catch { e ->
                    e.printStackTrace()
                }
                .collect { tasks ->
                    _downloadingTasks.value = tasks
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            TaskStore.completedTasks
                .catch { e ->
                    e.printStackTrace()
                }
                .collect { tasks ->
                    _completedTasks.value = tasks
                }
        }
    }
}

