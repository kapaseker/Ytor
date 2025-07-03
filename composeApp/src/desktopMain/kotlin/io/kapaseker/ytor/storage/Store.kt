package io.kapaseker.ytor.storage

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.extensions.storeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class DestinationHistory(val items: List<Destination>)

@Serializable
data class Destination(val path: String, val addTime: Long)

object Store {

    init {
        File("./cache/").mkdirs()
    }

    private val store: KStore<DestinationHistory> =
        storeOf(file = Path("./cache/destinations.json"), version = 0)

    val destinationHistory: Flow<DestinationHistory> = store.updates.filterNotNull()

    suspend fun addDestination(item: String) {
        if (item.isBlank()) {
            return
        }
        store.update { history ->
            val newItem = Destination(item, System.currentTimeMillis())
            val olds = history?.items?.toMutableList() ?: mutableListOf()
            olds.removeIf { it.path == item }
            olds.add(0, newItem)
            history?.copy(items = olds) ?: DestinationHistory(olds)
        }
    }

    suspend fun remove(item: String) {
        store.update { history ->
            val newItems = history?.items?.toMutableList()
            newItems?.removeIf { it.path == item }
            val updatedList = newItems.orEmpty()
            history?.copy(items = updatedList) ?: DestinationHistory(updatedList)
        }
    }
}