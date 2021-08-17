package com.example.foodist.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.Venues
import com.example.foodist.domain.repositories.VenueRepository
import com.example.foodist.services.LocationService
import com.example.foodist.services.PermissionsRequest
import com.example.foodist.services.PermissionsService
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


  private fun getVenues(latLng: LatLng, radius: Float) = viewModelScope.launch {
    _venueRequestStatus.value = Status.LOADING

    val radius = MapMeasurements().getRadius(latLng as LatLng, radius.toDouble())
    venueRepository.fetchVenues(latLng, radius).let { venueResult ->
      when (venueResult) {
        is ResultWrapper.NetworkError -> _venueRequestStatus.value = Status.ERROR_NETWORK
        is ResultWrapper.GenericError -> _venueRequestStatus.value = Status.ERROR
        is ResultWrapper.Success -> {
          val oldList = if (_venues.value?.isNotEmpty() == true) _venues.value else listOf<Venue>()
          var finalResultSet = venueResult.value.toMutableList()
          if (oldList != null) {
            finalResultSet.addAll(oldList)
            finalResultSet.distinctBy { it.id }
          }

          _venues.value = finalResultSet
          _venueRequestStatus.value = Status.SUCCESS
        }
      }
    }
  }

  private fun getCurrentLocation() {
    viewModelScope.launch {
      locationService.fetchLocation().let {
        _currentCenterPoint.value = it
        _setMapArea.value = true
      }
    }
  }

  private fun setCurrentLocation(latLng: LatLng) {
    locationService.setLocation(latLng)
  }

  companion object {
    const val DEFAULT_ZOOM_LEVEL = 15.0f
  }
}