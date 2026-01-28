package com.simplstudios.simplstream

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp

/**
 * SimplStream Application
 * By SimplStudios
 */
@HiltAndroidApp
class SimplStreamApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
    }

    // Configure Coil image loader for optimal TV performance
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% of app memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05) // 5% of disk space
                    .build()
            }
            .crossfade(true)
            .crossfade(300)
            .build()
    }
}
