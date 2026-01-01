package io.kapaseker.ytor.storage

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.kapaseker.ytor.database.Destination as DestinationRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DestinationHistory(val items: List<Destination>)

data class Destination(val path: String, val addTime: Long)

object Store {
    private val database = Database
    
    val destinationHistory: Flow<DestinationHistory> = database
        .getAllDestinations()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows ->
            val destinations = rows.map { row ->
                Destination(
                    path = row.path,
                    addTime = row.add_time
                )
            }
            DestinationHistory(destinations)
        }

    fun addDestination(item: String) {
        if (item.isBlank()) {
            return
        }
        // 使用 INSERT OR REPLACE 更新记录，如果已存在则更新 add_time，让最新的记录排在顶部
        database.insertDestination(item, System.currentTimeMillis())
    }

    fun remove(item: String) {
        database.deleteDestination(item)
    }
}