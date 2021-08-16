package com.example.foodist.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.core.app.ActivityCompat
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodist.domain.repositories.GeolocationRepository
import com.example.foodist.utils.Constants.Companion.LAST_LOCATION_SHARED_PREFERENCE_KEY
import com.example.foodist.utils.CountryList
import com.example.foodist.utils.ResultWrapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Operating Logic when fetching current location:
 *   -  If the user has given location permissions, then fetch via locationProvider
 *   -  If the user has not given location permissions, then fetch via geolocationRepository
 *   -  If there is a network connection issue fetch the lastViewed location
 *   -  If all else fails fetch the center of the users country via device locale and list of centroid points as starting point
 */
class LocationService @Inject constructor(
  val geoLocation: GeolocationRepository,
  @ApplicationContext val context: Context
) {
  private var fusedLocationProviderClient: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(context)

  private val _currentLocation = MutableLiveData<LatLng>()
  val currentLocation: LiveData<LatLng> = _currentLocation

  fun setLocation(latLng: LatLng) {
    val sharedPreferences = context.getSharedPreferences(
      LAST_LOCATION_SHARED_PREFERENCE_KEY,
      Context.MODE_PRIVATE
    )

    with(sharedPreferences.edit()) {
      putString(
        LAST_LOCATION_SHARED_PREFERENCE_KEY,
        "${latLng.latitude},${latLng.longitude}"
      )
      apply()
    }
  }

  suspend fun fetchLocation(): LatLng {
    if (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      fusedLocationProviderClient.lastLocation.continueWithTask {
        it
      }.await().let { result ->
        if (result != null) {
          return LatLng(result.latitude, result.longitude)
        }
      }
    }

    geoLocation.fetchLocation().let { result ->
      if (result is ResultWrapper.Success) {
        return result.value
      }
    }

    return fetchLocationFromSharedPreferences() ?: (fetchLocationFromUserLocale() ?: FALLBACK_LAT_LNG)
  }

  private fun fetchLocationFromSharedPreferences(): LatLng? {
    val sharedPreferencesResult = context.getSharedPreferences(
      LAST_LOCATION_SHARED_PREFERENCE_KEY,
      Context.MODE_PRIVATE
    ).getString(LAST_LOCATION_SHARED_PREFERENCE_KEY, "")

    val location = sharedPreferencesResult?.split(",")
    if (location?.size != 2) {
      return null
    }

    return LatLng(location[0].toDouble(), location[1].toDouble())
  }

  private fun fetchLocationFromUserLocale(): LatLng? {
    val currentLocale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
    val country = CountryList[currentLocale.country.toString()] ?: return null
    return LatLng(country.latitude, country.longitude)
  }

  companion object {
    val FALLBACK_LAT_LNG = LatLng(40.7484, -73.9857)
  }
}