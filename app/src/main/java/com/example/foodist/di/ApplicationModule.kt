package com.example.foodist.di

import android.util.LruCache
import com.example.foodist.domain.models.Venue
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

  @Provides
  fun provideMaxSize(): Int = ((Runtime.getRuntime().maxMemory() / 1024) / 8).toInt()


  @Provides
  @Singleton
  fun provideInMemoryCache(maxSize: Int): LruCache<String, Venue> {
    return LruCache<String, Venue>(maxSize)
  }
}