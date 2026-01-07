package io.kapaseker.ytor.page.start.biz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.kapaseker.ytor.storage.Store
import io.kapaseker.ytor.util.DownloadState
import io.kapaseker.ytor.util.YtDownloader
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import io.kapaseker.ytor.util.DownloadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StartViewModel : ViewModel() {

    private val _destinationHistory = MutableStateFlow<ImmutableList<String>>(persistentListOf())
    val destinationHistory: StateFlow<ImmutableList<String>> = _destinationHistory.asStateFlow()

    // 从 YtDownloader 获取下载状态，如果没有任务则返回 Idle 状态
    val downloadState: StateFlow<DownloadState> = YtDownloader.downloadStateFlow
        .map { it ?: DownloadState(status = DownloadStatus.Idle) }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadState(status = DownloadStatus.Idle)
        )

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

    /**
     * 触发下载任务
     */
    fun download(input: String, dir: String) {
        YtDownloader.download(input, dir)
    }
}