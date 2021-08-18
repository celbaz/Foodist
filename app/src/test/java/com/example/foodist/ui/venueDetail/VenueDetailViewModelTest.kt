package com.example.foodist.ui.venueDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.foodist.domain.models.Location
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.VenueDetails
import com.example.foodist.domain.repositories.VenueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
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
class VenueDetailViewModelTest {

  @InjectMocks
  private lateinit var viewModel: VenueDetailViewModel

  @Mock
  private lateinit var observer: Observer<VenueDetails>

  @Mock
  private lateinit var mockRepository: VenueRepository

  private val mainThreadSurrogate = newSingleThreadContext("UI thread")

  @Before
  fun setUp() {
    Dispatchers.setMain(mainThreadSurrogate)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mainThreadSurrogate.close()
  }

  @get:Rule
  var rule: TestRule = InstantTaskExecutorRule()


  @Test
  fun onViewReadyMissingCachedValue() {
    Mockito.`when`(mockRepository.getVenueFromCache("")).thenReturn(null)
    viewModel = VenueDetailViewModel(mockRepository)
    viewModel.onViewReady("")
    Assert.assertEquals(null, viewModel.cachedInformation.value)
  }

  @Test
  fun onViewReadySuccessCachedValue() {
    val location = Location("Nowhere", 0.0, 0.0, "", "", "", "", listOf())
    val venue = Venue("123", "restaurant", location)
    Mockito.`when`(mockRepository.getVenueFromCache("123")).thenReturn(venue)
    viewModel = VenueDetailViewModel(mockRepository)
    viewModel.onViewReady("123")
    Assert.assertEquals(venue, viewModel.cachedInformation.value)
  }
}