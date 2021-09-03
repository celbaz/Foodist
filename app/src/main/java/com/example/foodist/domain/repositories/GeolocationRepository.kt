package com.example.foodist.domain.repositories

import com.example.foodist.data.network.geolocation.GoogleMapsApiService
import com.example.foodist.data.network.geolocation.model.GeolocationRequest
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.Telephony
import com.example.foodist.utils.handleThrowable
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GeolocationRepository @Inject constructor(
  private val apiService: GoogleMapsApiService,
  private val telephony: Telephony,
) {
  suspend fun fetchLocation(): ResultWrapper<LatLng> {
    val radioType = telephony.getTelephonyRadioType()

    try {
      apiService.fetchGeolocation(GeolocationRequest(radioType)).let { response ->
        val result = response.body()
        return if (result != null) {
          ResultWrapper.Success(LatLng(result.location.lat, result.location.lng))
        } else {
          ResultWrapper.GenericError(response.code())
        }
      }
    } catch (throwable: Throwable) {
      return handleThrowable(throwable)
    }
  }
}