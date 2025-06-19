package io.kapaseker.ytor.storage

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.extensions.storeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class DestinationHistory(val items:List<Destination>)

@Serializable
data class Destination(val name: String, val addTime: Long)

object Store {

    init {
        File("./cache/").mkdirs()
    }

    private val store: KStore<DestinationHistory> = storeOf(file = Path("./cache/destinations.json"), version = 0)

    val destinationHistory : Flow<DestinationHistory> = store.updates.filterNotNull()

    suspend fun addDestination(destination: String) {
        store.update { history ->
            val newItem = Destination(destination, System.currentTimeMillis())
            val olds = history?.items?.toMutableList() ?: mutableListOf()
            olds.removeIf { it.name == destination }
            olds.add(0, newItem)
            history?.copy(items = olds) ?: DestinationHistory(olds)
        }
    }
}