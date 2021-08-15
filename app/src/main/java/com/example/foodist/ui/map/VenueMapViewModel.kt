package com.example.foodist.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodist.domain.models.Venues
import com.example.foodist.domain.repositories.VenueRepository
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
) : ViewModel() {
  private val _venues = MutableLiveData<Venues>()
  private val _venueRequestStatus = MutableLiveData<Status>()
  private val _currentCenterPoint = MutableLiveData<LatLng>()
  private val _zoom = MutableLiveData(DEFAULT_ZOOM_LEVEL)

  val zoom: LiveData<Float> = _zoom
  val venues: LiveData<Venues> = _venues
  val currentCenterPoint = _currentCenterPoint
  val venueRequestStatus: LiveData<Status> = _venueRequestStatus

  private fun getVenues(latLng: LatLng, radius: Double) = viewModelScope.launch {
    _venueRequestStatus.value = Status.LOADING

    venueRepository.fetchVenues(listOf(latLng.latitude, latLng.longitude), radius).let { venueResult ->
      when (venueResult) {
        is ResultWrapper.NetworkError -> _venueRequestStatus.value = Status.ERROR_NETWORK
        is ResultWrapper.GenericError -> _venueRequestStatus.value = Status.ERROR
        is ResultWrapper.Success -> _venues.value = venueResult.value
      }
    }
  }

  fun onMapReady(latLng: LatLng, radius: Double)  {
    getVenues(latLng, radius)
  }

  fun onMapMovement(centerPoint: LatLng, currentZoom: Float) {
    var mapMoved = false

    if (_zoom.value != currentZoom) {
      _zoom.value = currentZoom
      mapMoved = true
    }

    if (centerPoint.latitude != _currentCenterPoint.value?.latitude ||
      centerPoint.longitude != _currentCenterPoint.value?.longitude
    ) {
      _currentCenterPoint.value = centerPoint
      mapMoved = true
    }

    if (mapMoved) {
      val radius = MapMeasurements().getRadius(centerPoint, currentZoom.toDouble())
      getVenues(centerPoint, radius)
    }
  }

  companion object {
    const val DEFAULT_ZOOM_LEVEL = 15.0f
  }
}