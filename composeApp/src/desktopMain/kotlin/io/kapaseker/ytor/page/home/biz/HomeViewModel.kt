package io.kapaseker.ytor.page.home.biz

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.kapaseker.ytor.storage.Store
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    init {
//        val p = Runtime.getRuntime().exec(arrayOf("ffmpeg","-version"))
//        val text = p.inputStream.bufferedReader().readLine()
//        println(text)
    }

    private val _destinationHistory = MutableStateFlow<ImmutableList<String>>(persistentListOf())
    val destinationHistory: StateFlow<ImmutableList<String>> = _destinationHistory.asStateFlow()

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
        println("down load input: $input")

        val saveOption = dir.takeIf { it.isNotEmpty() } ?: "./"

        // yt-dlp -f bestvideo[height<=1080][ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best https://youtu.be/zEC2UhUJvMA?si=BkL4Kp3ow-tRz77C
        viewModelScope.launch(Dispatchers.IO) {

            Store.addDestination(dir)

//            val good = Runtime.getRuntime().exec(arrayOf("yt-dlp", "-P", saveOption, "-f", "bestvideo[height<=1080][ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best", input))
//
//            good.inputStream.bufferedReader().forEachLine {
//                println(it)
//            }
//
//            good.waitFor()
//
//            println("done")
        }
    }
}