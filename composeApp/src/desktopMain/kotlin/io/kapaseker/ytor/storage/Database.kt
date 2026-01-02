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
            // 数据库文件存在，检查download_task表是否存在
            try {
                // 尝试查询download_task表是否存在
                db.downloadTaskQueries.getDownloadingTasks().executeAsList()
            } catch (e: Exception) {
                // download_task表不存在，调用Schema.create创建所有表
                // SQLDelight的Schema.create会使用CREATE TABLE IF NOT EXISTS，不会破坏现有表
                YtorDatabase.Schema.create(driver)
            }
        }
        db
    }
    
    val destinationQueries = database.destinationQueries
    val downloadTaskQueries = database.downloadTaskQueries
    
    fun getAllDestinations(): Query<DestinationRow> = destinationQueries.selectAll()
    
    fun insertDestination(path: String, addTime: Long) {
        destinationQueries.insertOrReplace(path, addTime)
    }
    
    fun deleteDestination(path: String) {
        destinationQueries.deleteByPath(path)
    }
}

