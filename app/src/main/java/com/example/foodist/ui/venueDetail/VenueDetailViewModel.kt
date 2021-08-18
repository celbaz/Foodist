package com.example.foodist.ui.venueDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.VenueDetails
import com.example.foodist.domain.repositories.VenueRepository
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VenueDetailViewModel @Inject constructor(var venueRepository: VenueRepository) : ViewModel() {
  private val _cachedInformation = MutableLiveData<Venue>()
  private val _requestStatus = MutableLiveData<Status>()
  private val _venueDetails = MutableLiveData<VenueDetails>()

  var cachedInformation: LiveData<Venue> = _cachedInformation
  var venueDetails = _venueDetails
  val requestStatus: LiveData<Status> = _requestStatus

  fun onViewReady(id: String) {
    val venue = venueRepository.getVenueFromCache(id)
    if (venue != null) {
      _cachedInformation.value = venue
    }

    viewModelScope.launch {
      _requestStatus.value = Status.LOADING
      when (val result = venueRepository.getVenueDetails(id)) {
        is ResultWrapper.NetworkError -> _requestStatus.value = Status.ERROR_NETWORK
        is ResultWrapper.GenericError -> _requestStatus.value = Status.ERROR
        is ResultWrapper.Success -> {
          _requestStatus.value = Status.SUCCESS
          _venueDetails.value = result.value
        }
      }
    }
  }

}