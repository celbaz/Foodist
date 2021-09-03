package com.example.foodist.ui.venueDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.foodist.domain.models.Location
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.VenueDetails
import com.example.foodist.domain.repositories.VenueRepository
import com.example.foodist.utils.ResultWrapper
import com.example.foodist.utils.Status
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

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class VenueDetailViewModelTest {
  @InjectMocks
  private lateinit var viewModel: VenueDetailViewModel

  @Mock
  private lateinit var mockRepository: VenueRepository

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
  fun onViewReady_missingCachedValue() {
    Mockito.`when`(mockRepository.getVenueFromCache("")).thenReturn(null)

    viewModel = VenueDetailViewModel(mockRepository)
    viewModel.onViewReady("")

    Assert.assertEquals(null, viewModel.cachedInformation.value)
  }

  @Test
  fun onViewReady_successCachedValue() {
    val location = Location("Nowhere", 0.0, 0.0, "", "", "", "", listOf())
    val venue = Venue("123", "restaurant", location)
    Mockito.`when`(mockRepository.getVenueFromCache("123")).thenReturn(venue)

    viewModel = VenueDetailViewModel(mockRepository)
    viewModel.onViewReady("123")

    Assert.assertEquals(venue, viewModel.cachedInformation.value)
  }

  @Test
  fun onViewReady_fetchedSuccess() {
    val venueDetails = VenueDetails(
      "123",
      null,
      null,
      null,
      null,
      null,
      null
    )

    runBlocking {
      Mockito.`when`(mockRepository.getVenueDetails("123")).thenReturn(ResultWrapper.Success(venueDetails))
    }

    viewModel = VenueDetailViewModel(mockRepository)
    viewModel.onViewReady("123")

    Assert.assertEquals(venueDetails, viewModel.venueDetails.value)
    Assert.assertEquals(Status.SUCCESS, viewModel.requestStatus.value)
  }

  @Test
  fun onViewReady_fetchedErrorGeneric() {
    runBlocking {
      Mockito.`when`(mockRepository.getVenueDetails("123")).thenReturn(ResultWrapper.GenericError())
    }

    viewModel = VenueDetailViewModel(mockRepository)
    viewModel.onViewReady("123")

    Assert.assertEquals(null, viewModel.venueDetails.value)
    Assert.assertEquals(Status.ERROR, viewModel.requestStatus.value)
  }

  @Test
  fun onViewReady_fetchedErrorNetwork() {
    runBlocking {
      Mockito.`when`(mockRepository.getVenueDetails("123")).thenReturn(ResultWrapper.NetworkError)
    }

    viewModel = VenueDetailViewModel(mockRepository)
    viewModel.onViewReady("123")

    Assert.assertEquals(null, viewModel.venueDetails.value)
    Assert.assertEquals(Status.ERROR_NETWORK, viewModel.requestStatus.value)
  }
}