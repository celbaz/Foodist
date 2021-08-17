package com.example.foodist.domain.repositories

import android.util.Log
import android.util.LruCache
import com.example.foodist.data.network.foursquare.FoursquareApiService
import com.example.foodist.data.network.foursquare.mappers.VenueMapper
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.Venues
import com.example.foodist.utils.Constants
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.handleThrowable
import javax.inject.Inject

class VenueRepository @Inject constructor(
  private val apiService: FoursquareApiService,
  private val cache: LruCache<String, Venue>
) {
  suspend fun fetchVenues(coordinates: List<Double>, radius: Double): ResultWrapper<Venues> {
    val noInternetConnection = false
    if (noInternetConnection) {
      return ResultWrapper.NetworkError
    }

    return try {
      val results = apiService.fetchVenues(
          coordinates.joinToString(","),
          radius,
          listOf(Constants.FOURSQUARE_FOOD_CATEGORY_ID)
        )

      val resultsMapped = VenueMapper().mapVenueResponseToDomain(results)
      resultsMapped.forEach { venue ->
        cache.put(venue.id, venue)
      }

      ResultWrapper.Success(resultsMapped)
    } catch (throwable: Throwable) {
      handleThrowable(throwable)
    }

  }
}