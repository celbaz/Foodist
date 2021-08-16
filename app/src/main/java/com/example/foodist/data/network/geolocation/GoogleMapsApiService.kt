package com.example.foodist.data.network.geolocation

import com.example.foodist.data.network.geolocation.model.GeolocationRequest
import com.example.foodist.data.network.geolocation.model.GeolocationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface GoogleMapsApiService {
  @Headers("Content-Type: application/json")
  @POST("geolocate")
  suspend fun fetchGeolocation(@Body body: GeolocationRequest): Response<GeolocationResponse>
}