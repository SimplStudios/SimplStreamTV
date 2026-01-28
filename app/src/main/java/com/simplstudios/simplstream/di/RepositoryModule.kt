package com.simplstudios.simplstream.di

import com.simplstudios.simplstream.data.repository.ContentRepositoryImpl
import com.simplstudios.simplstream.data.repository.ProfileRepositoryImpl
import com.simplstudios.simplstream.data.repository.WatchHistoryRepositoryImpl
import com.simplstudios.simplstream.data.repository.WatchlistRepositoryImpl
import com.simplstudios.simplstream.domain.repository.ContentRepository
import com.simplstudios.simplstream.domain.repository.ProfileRepository
import com.simplstudios.simplstream.domain.repository.WatchHistoryRepository
import com.simplstudios.simplstream.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository
    
    @Binds
    @Singleton
    abstract fun bindContentRepository(
        impl: ContentRepositoryImpl
    ): ContentRepository
    
    @Binds
    @Singleton
    abstract fun bindWatchHistoryRepository(
        impl: WatchHistoryRepositoryImpl
    ): WatchHistoryRepository
    
    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(
        impl: WatchlistRepositoryImpl
    ): WatchlistRepository
}
