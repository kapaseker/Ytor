package io.kapaseker.ytor.storage

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kapaseker.ytor.database.Destination as DestinationRow
import io.kapaseker.ytor.database.YtorDatabase
import java.io.File

object Database {
    private val cacheDir: File = File("./cache/").apply { mkdirs() }
    private val dbFile: File = File(cacheDir, "ytor.db")

    private val driver: SqlDriver = JdbcSqliteDriver(
        url = "jdbc:sqlite:${dbFile.absolutePath}"
    )

    private val database: YtorDatabase = run {
        val db = YtorDatabase(driver)
        
        // 初始化数据库 schema
        if (!dbFile.exists() || dbFile.length() == 0L) {
            YtorDatabase.Schema.create(driver)
        } else {
            // 数据库文件存在，尝试查询表是否存在
            try {
                // 尝试执行一个简单的查询来检查表是否存在
                db.destinationQueries.count().executeAsOne()
            } catch (e: Exception) {
                // 表不存在或查询失败，创建 schema
                YtorDatabase.Schema.create(driver)
            }
        }
        db
    }
    
    val destinationQueries = database.destinationQueries
    
    fun getAllDestinations(): Query<DestinationRow> = destinationQueries.selectAll()
    
    fun insertDestination(path: String, addTime: Long) {
        destinationQueries.insertOrReplace(path, addTime)
    }
    
    fun deleteDestination(path: String) {
        destinationQueries.deleteByPath(path)
    }
}

