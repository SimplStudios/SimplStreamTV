package com.simplstudios.simplstream.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for different referer configurations
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Movies111DataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VidSrcDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VidnestDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VidlinkDataSource

/**
 * Hilt module providing Media3 ExoPlayer components with header spoofing
 * for different video sources
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /**
     * Creates an HttpDataSource.Factory with custom headers for a specific source
     */
    @OptIn(UnstableApi::class)
    private fun createDataSourceFactory(referer: String, origin: String): HttpDataSource.Factory {
        return DefaultHttpDataSource.Factory()
            .setUserAgent(USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)
            .setDefaultRequestProperties(mapOf(
                "Referer" to referer,
                "Origin" to origin,
                "Accept" to "*/*",
                "Accept-Language" to "en-US,en;q=0.9",
                "Sec-Fetch-Dest" to "empty",
                "Sec-Fetch-Mode" to "cors",
                "Sec-Fetch-Site" to "cross-site"
            ))
    }

    @Provides
    @Singleton
    @Movies111DataSource
    @OptIn(UnstableApi::class)
    fun provideMovies111DataSourceFactory(): HttpDataSource.Factory {
        return createDataSourceFactory(
            referer = "https://111movies.com/",
            origin = "https://111movies.com"
        )
    }

    @Provides
    @Singleton
    @VidSrcDataSource
    @OptIn(UnstableApi::class)
    fun provideVidSrcDataSourceFactory(): HttpDataSource.Factory {
        return createDataSourceFactory(
            referer = "https://vidsrc-embed.ru/",
            origin = "https://vidsrc-embed.ru"
        )
    }

    @Provides
    @Singleton
    @VidnestDataSource
    @OptIn(UnstableApi::class)
    fun provideVidnestDataSourceFactory(): HttpDataSource.Factory {
        return createDataSourceFactory(
            referer = "https://vidnest.fun/",
            origin = "https://vidnest.fun"
        )
    }

    @Provides
    @Singleton
    @VidlinkDataSource
    @OptIn(UnstableApi::class)
    fun provideVidlinkDataSourceFactory(): HttpDataSource.Factory {
        return createDataSourceFactory(
            referer = "https://vidlink.pro/",
            origin = "https://vidlink.pro"
        )
    }

    /**
     * Default ExoPlayer instance with generic configuration
     * Individual players should be created per-use for specific sources
     */
    @Provides
    @OptIn(UnstableApi::class)
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        @Movies111DataSource dataSourceFactory: HttpDataSource.Factory
    ): ExoPlayer {
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setSeekBackIncrementMs(10000) // 10s Rewind
            .setSeekForwardIncrementMs(10000) // 10s Fast Forward
            .build()
    }
}
