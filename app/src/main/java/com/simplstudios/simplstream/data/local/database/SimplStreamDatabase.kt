package com.simplstudios.simplstream.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.simplstudios.simplstream.data.local.dao.ProfileDao
import com.simplstudios.simplstream.data.local.dao.WatchHistoryDao
import com.simplstudios.simplstream.data.local.dao.WatchlistDao
import com.simplstudios.simplstream.data.local.entity.ProfileEntity
import com.simplstudios.simplstream.data.local.entity.WatchHistoryEntity
import com.simplstudios.simplstream.data.local.entity.WatchlistEntity

@Database(
    entities = [
        ProfileEntity::class,
        WatchHistoryEntity::class,
        WatchlistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SimplStreamDatabase : RoomDatabase() {
    
    abstract fun profileDao(): ProfileDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun watchlistDao(): WatchlistDao
    
    companion object {
        const val DATABASE_NAME = "simplstream_db"
    }
}
