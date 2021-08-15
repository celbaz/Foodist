package com.example.foodist.di

import com.example.foodist.BuildConfig
import com.example.foodist.data.network.foursquare.FoursquareApiService
import com.example.foodist.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Provides
  fun provideMoshiConverterFactory(): MoshiConverterFactory =
    MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build())

  @Provides
  fun provideOkHttpClient(): OkHttpClient = okhttp3.OkHttpClient.Builder().addInterceptor { chain ->
    val url = chain
      .request()
      .url()
      .newBuilder()
      .addQueryParameter("client_id", BuildConfig.FOURSQUARE_CLIENT_ID)
      .addQueryParameter("client_secret", BuildConfig.FOURSQUARE_CLIENT_SECRET)
      .addQueryParameter("v", Constants.FOURSQUARE_VERSION_DATE)
      .build()
    chain.proceed(chain.request().newBuilder().url(url).build())
  }.build()

  @Singleton
  @Provides
  fun provideRetrofit(
    moshiConverterFactory: MoshiConverterFactory,
    okHttpClient: OkHttpClient
  ): Retrofit {
    return Retrofit.Builder()
      .baseUrl(Constants.FOURSQUARE_BASE_URL)
      .addConverterFactory(moshiConverterFactory)
      .client(okHttpClient)
      .build()
  }

  @Provides
  @Singleton
  fun provideFoursquareApiService(retrofit: Retrofit): FoursquareApiService = retrofit.create(
    FoursquareApiService::class.java
  )
}