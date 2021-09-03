package com.example.foodist.di

import android.content.Context
import android.util.LruCache
import com.example.foodist.domain.models.Venue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

  @Provides
  fun provideLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
    return LocationServices.getFusedLocationProviderClient(context)
  }

  @Provides
  @Named("MaxCacheSize")
  fun provideMaxSize(): Int = ((Runtime.getRuntime().maxMemory() / 1024) / 8).toInt()

  @Provides
  @Singleton
  fun provideInMemoryCache(@Named("MaxCacheSize") maxSize: Int): LruCache<String, Venue> {
    return LruCache<String, Venue>(maxSize)
  }
}