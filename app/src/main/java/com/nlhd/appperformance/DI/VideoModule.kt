package com.nlhd.appperformance.DI

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VideoModule {

    private const val CACHE_SIZE = 1_000L * 1024 * 1024

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideSimpleCache(@ApplicationContext context: Context): SimpleCache {
        return SimpleCache(
            File(context.cacheDir, "media"),
            LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
            ExoDatabaseProvider(context)
        )
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideCacheDataSourceFactory(simpleCache: SimpleCache): CacheDataSource.Factory {
        return CacheDataSource.Factory().apply {
            setCache(simpleCache)
            setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideDefaultMediaSourceFactory(cacheDataSourceFactory: CacheDataSource.Factory): DefaultMediaSourceFactory {
        return DefaultMediaSourceFactory(cacheDataSourceFactory)
    }
}