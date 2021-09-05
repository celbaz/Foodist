package com.example.foodist.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodist.domain.models.Venues
import com.example.foodist.domain.repositories.VenueRepository
import com.example.foodist.services.LocationService
import com.example.foodist.services.PermissionsRequest
import com.example.foodist.services.PermissionsService
import com.example.foodist.utils.Constants.Companion.DEFAULT_ZOOM_LEVEL
import com.example.foodist.utils.MapMeasurements
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.Status
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class VenueMapViewModel @Inject constructor(
  var venueRepository: VenueRepository,
  var locationService: LocationService,
  var mapMeasurements: MapMeasurements,
) : ViewModel() {
  private val _venues = MutableLiveData<Venues>(mutableListOf())
  private val _venueRequestStatus = MutableLiveData<Status>()
  private val _currentCenterPoint = MutableLiveData<LatLng>()
  private val _setMapArea = MutableLiveData<Boolean>()
  private val _zoom = MutableLiveData(DEFAULT_ZOOM_LEVEL)

  val zoomValue get() = _zoom.value ?: DEFAULT_ZOOM_LEVEL
  val venues: LiveData<Venues> = _venues
  val currentCenterPoint: LiveData<LatLng> = _currentCenterPoint
  val setMapArea: LiveData<Boolean> = _setMapArea
  val venueRequestStatus: LiveData<Status> = _venueRequestStatus

  fun onMapReady(permissionService: PermissionsService) {
    permissionService.checkGrantedPermissions(PermissionsRequest.LOCATION) {
      getCurrentLocation()
    }
  }

  fun onMapMovement(centerPoint: LatLng, currentZoom: Float) {
    if (centerPoint.latitude != _currentCenterPoint.value?.latitude ||
      centerPoint.longitude != _currentCenterPoint.value?.longitude ||
      _zoom.value != currentZoom
    ) {
      _zoom.value = currentZoom
      _currentCenterPoint.value = centerPoint

      setCurrentLocation(centerPoint)
      getVenues(centerPoint, zoomValue)
    }
  }

  private fun getVenues(latLng: LatLng, zoom: Float) = viewModelScope.launch {
    _venueRequestStatus.value = Status.LOADING

    val radius = mapMeasurements.getRadius(latLng, zoom.toDouble())
    val cachedVenues = venueRepository.fetchVenuesFromCache(latLng, radius)
    _venues.value = cachedVenues

    venueRepository.fetchVenues(latLng, radius).let { venueResult ->
      when (venueResult) {
        is ResultWrapper.NetworkError -> _venueRequestStatus.value = Status.ERROR_NETWORK
        is ResultWrapper.GenericError -> _venueRequestStatus.value = Status.ERROR
        is ResultWrapper.Success -> {
          _venues.value = venueResult.value
          _venueRequestStatus.value = Status.SUCCESS
        }
      }
    }
  }

  private fun getCurrentLocation() {
    viewModelScope.launch {
      locationService.fetchLastKnownLocation().let { mapCenterPoint ->
        _currentCenterPoint.value = mapCenterPoint
        _setMapArea.value = true
        getVenues(mapCenterPoint, zoomValue)
      }
    }
  }

  private fun setCurrentLocation(latLng: LatLng) {
    locationService.setLocation(latLng)
  }
}