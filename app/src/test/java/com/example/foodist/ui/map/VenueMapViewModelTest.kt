package com.example.foodist.ui.map

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.foodist.domain.models.Location
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.Venues
import com.example.foodist.domain.repositories.VenueRepository
import com.example.foodist.services.LocationService
import com.example.foodist.services.OnCompleteCallback
import com.example.foodist.services.PermissionsRequest
import com.example.foodist.services.PermissionsService
import com.example.foodist.utils.Constants.Companion.DEFAULT_ZOOM_LEVEL
import com.example.foodist.utils.MapMeasurements
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.Status
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.stubbing.Answer

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class VenueMapViewModelTest {

  @InjectMocks
  private lateinit var viewModel: VenueMapViewModel

  @Mock
  private lateinit var mockVenueRepository: VenueRepository

  @Mock
  private lateinit var mockLocationService: LocationService

  @Mock
  private lateinit var mockMapMeasurements: MapMeasurements

  @Mock
  private lateinit var mockPermissionsService: PermissionsService

  @Mock
  private lateinit var mockVenuesObserver: Observer<Venues>

  private val mainThreadSurrogate = TestCoroutineDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(mainThreadSurrogate)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()

  @Test
  fun onMapReady_cachedAndRequestSuccess() {
    val location = LatLng(0.0, 0.0)
    val radius = 10.0
    val cachedResponse = listOf(
      Venue("123", "cached location", Location(null, 0.0, 0.0, null, null, null, null, null))
    )
    val rpcResponse = listOf(
      Venue("321", "requested location", Location(null, 0.0, 0.0, null, null, null, null, null))
    )

    Mockito.`when`(mockPermissionsService.checkGrantedPermissions(eq(PermissionsRequest.LOCATION), anyOrNull()))
      .thenAnswer(Answer { invocation -> invocation.getArgument<OnCompleteCallback>(1)(true) })

    runBlocking {
      Mockito.`when`(mockLocationService.fetchLocation()).thenReturn(location)
      Mockito.`when`(mockMapMeasurements.getRadius(location, DEFAULT_ZOOM_LEVEL.toDouble())).thenReturn(radius)
      Mockito.`when`(mockVenueRepository.fetchVenuesFromCache(location, radius)).thenReturn(cachedResponse)
      Mockito.`when`(mockVenueRepository.fetchVenues(location, radius)).thenReturn(ResultWrapper.Success(rpcResponse))
    }

    viewModel = VenueMapViewModel(mockVenueRepository, mockLocationService, mockMapMeasurements)
    viewModel.venues.observeForever(mockVenuesObserver)

    viewModel.onMapReady(mockPermissionsService)

    val inorder = inOrder(mockVenuesObserver)

    inorder.verify(mockVenuesObserver).onChanged(cachedResponse)
    inorder.verify(mockVenuesObserver).onChanged(rpcResponse)
    Assert.assertEquals(Status.SUCCESS, viewModel.venueRequestStatus.value)
  }
}