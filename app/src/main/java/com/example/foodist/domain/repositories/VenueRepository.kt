package com.example.foodist.domain.repositories

import android.util.LruCache
import com.example.foodist.data.network.foursquare.FoursquareApiService
import com.example.foodist.data.network.foursquare.mappers.VenueMapper
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.VenueDetails
import com.example.foodist.domain.models.Venues
import com.example.foodist.utils.*
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class VenueRepository @Inject constructor(
  private val apiService: FoursquareApiService,
  private val cache: LruCache<String, Venue>
) {
  suspend fun fetchVenues(coordinates: LatLng, radius: Double): ResultWrapper<Venues> {
    if (!isOnline()) {
      val cachedResults = fetchVenuesFromCache(coordinates, radius)
      return ResultWrapper.Success(cachedResults)
    }

    return try {
      val results = apiService.fetchVenues(
        "${coordinates.latitude},${coordinates.longitude}",
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

  fun fetchVenuesFromCache(coordinates: LatLng, radius: Double): Venues {
    val dimensions = MapMeasurements().getLatLngDimensionsFromRadius(coordinates, radius)
    val cachedResults = cache.snapshot().values.filter { venue ->
      (venue.location.lat >= dimensions.startPointLat && venue.location.lat <= dimensions.endPointLat) &&
          (venue.location.lng >= dimensions.startPointLng && venue.location.lng <= dimensions.endPointLng)
    }

    return cachedResults.toList()
  }

  suspend fun getVenueDetails(id: String): ResultWrapper<VenueDetails> {
    return try {
      val result = apiService.fetchVenueDetails(id)
      val resultMapped = VenueMapper().mapVenueDetailsResponseToDomain(result)
      ResultWrapper.Success(resultMapped)
    } catch (throwable: Throwable) {
      handleThrowable(throwable)
    }
  }

  fun getVenueFromCache(id: String): Venue? {
    return cache.get(id)
  }
}