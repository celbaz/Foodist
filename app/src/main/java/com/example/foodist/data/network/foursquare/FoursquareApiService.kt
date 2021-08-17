package com.example.foodist.data.network.foursquare

import com.example.foodist.data.network.foursquare.model.VenueDetailsResponse
import com.example.foodist.data.network.foursquare.model.VenueSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoursquareApiService {

  @GET("search")
  suspend fun fetchVenues(
    @Query("ll") coordinates: String,
    @Query("radius") radius: Double,
    @Query("categoryId") categories: List<String>
  ): Response<VenueSearchResponse>

  @GET("{id}")
  suspend fun fetchVenueDetails(@Path(value = "id") id: String): Response<VenueDetailsResponse>
}