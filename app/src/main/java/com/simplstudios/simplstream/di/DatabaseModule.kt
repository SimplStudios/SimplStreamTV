package com.simplstudios.simplstream.di

import android.content.Context
import androidx.room.Room
import com.simplstudios.simplstream.data.local.dao.ProfileDao
import com.simplstudios.simplstream.data.local.dao.WatchHistoryDao
import com.simplstudios.simplstream.data.local.dao.WatchlistDao
import com.simplstudios.simplstream.data.local.database.SimplStreamDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SimplStreamDatabase {
        return Room.databaseBuilder(
            context,
            SimplStreamDatabase::class.java,
            SimplStreamDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideProfileDao(database: SimplStreamDatabase): ProfileDao {
        return database.profileDao()
    }
    
    @Provides
    @Singleton
    fun provideWatchHistoryDao(database: SimplStreamDatabase): WatchHistoryDao {
        return database.watchHistoryDao()
    }
    
    @Provides
    @Singleton
    fun provideWatchlistDao(database: SimplStreamDatabase): WatchlistDao {
        return database.watchlistDao()
    }
}
