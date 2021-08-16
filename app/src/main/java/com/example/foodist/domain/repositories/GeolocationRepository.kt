package com.example.foodist.domain.repositories

import android.content.Context
import android.telephony.TelephonyManager
import com.example.foodist.data.network.geolocation.GoogleMapsApiService
import com.example.foodist.data.network.geolocation.model.GeolocationRequest
import com.example.foodist.data.network.geolocation.model.GeolocationResponse
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.handleThrowable
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GeolocationRepository @Inject constructor(
  private val apiService: GoogleMapsApiService,
  @ApplicationContext val context: Context
) {
  suspend fun fetchLocation(): ResultWrapper<LatLng> {
    val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    val phoneType = when (telephony.phoneType) {
      TelephonyManager.PHONE_TYPE_NONE -> "nr"
      TelephonyManager.PHONE_TYPE_CDMA -> "cdma"
      TelephonyManager.PHONE_TYPE_GSM -> "gsm"
      else -> "lte"
    }

    try {
      apiService.fetchGeolocation(GeolocationRequest(phoneType)).let { response ->
        val result = response.body()
        return if (result != null) {
          ResultWrapper.Success(LatLng(result.location.lat, result.location.lng))
        } else {
          ResultWrapper.GenericError()
        }
      }
    } catch (throwable: Throwable) {
      return handleThrowable(throwable)
    }
  }
}