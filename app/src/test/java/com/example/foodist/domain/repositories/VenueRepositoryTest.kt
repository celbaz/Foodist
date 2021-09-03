package com.example.foodist.domain.repositories

import android.util.LruCache
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.foodist.data.network.foursquare.FoursquareApiService
import com.example.foodist.data.network.foursquare.model.*
import com.example.foodist.domain.models.*
import com.example.foodist.domain.models.Contact
import com.example.foodist.domain.models.Stats
import com.example.foodist.utils.Constants
import com.example.foodist.utils.InternetConnectivity
import com.example.foodist.utils.ResultWrapper
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.HttpException
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class VenueRepositoryTest {

  @InjectMocks
  private lateinit var repository: VenueRepository

  @Mock
  private lateinit var apiServiceMock: FoursquareApiService

  @Mock
  private lateinit var lruCache: LruCache<String, Venue>

  @Mock
  private lateinit var internetConnectivityMock: InternetConnectivity


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
  fun fetchVenues_successOnline() {
    val venueSearchResponse = VenueSearchResponse(
      VenueSearchResults(
        listOf(
          VenueResponse(
            "venueId",
            "Venue Name",
            LocationResponse(null, null, 1.0, 2.0, null, null, null, null, null, null),
            null
          )
        )
      )
    )

    val expected = listOf(
      Venue(
        "venueId",
        "Venue Name",
        Location(null, 1.0, 2.0, null, null, null, null, null),
      )
    )

    val latLng = LatLng(1.0, 2.0)

    runBlocking {
      `when`(internetConnectivityMock.isOnline()).thenReturn(true)
      `when`(
        apiServiceMock.fetchVenues(
          "${latLng.latitude},${latLng.longitude}",
          1.0,
          listOf(
            Constants.FOURSQUARE_FOOD_CATEGORY_ID
          )
        )
      ).thenReturn(Response.success(venueSearchResponse))

      val result = repository.fetchVenues(latLng, 1.0)

      Assert.assertEquals(ResultWrapper.Success(expected), result)
    }
  }

  @Test
  fun fetchVenues_successOffline() {
    val latLng = LatLng(1.0, 2.0)

    runBlocking {
      `when`(internetConnectivityMock.isOnline()).thenReturn(false)
      val result = repository.fetchVenues(latLng, 1.0)

      Assert.assertEquals(ResultWrapper.Success(listOf<Venue>()), result)
    }
  }

  @Test
  fun fetchVenues_error() {
    val latLng = LatLng(1.0, 2.0)

    runBlocking {
      `when`(internetConnectivityMock.isOnline()).thenReturn(true)
      `when`(
        apiServiceMock.fetchVenues(
          "${latLng.latitude},${latLng.longitude}",
          1.0,
          listOf(
            Constants.FOURSQUARE_FOOD_CATEGORY_ID
          )
        )
      ).thenThrow(
        HttpException(
          Response.error<ResponseBody>(
            500,
            "error".toResponseBody("plain/text".toMediaTypeOrNull())
          )
        )
      )

      val result = repository.fetchVenues(latLng, 1.0)

      Assert.assertEquals(ResultWrapper.GenericError(500), result)
    }
  }

  @Test
  fun getVenueDetails_success() {
    val response = VenueDetailsResponse(
      VenueDetailResult(
        VenueDetail(
          "testVenue",
          null,
          LocationResponse(null, null, 1.0, 2.0, null, null, null, null, null, null),
          null,
          null,
          4.5,
          null,
          null
        )
      )
    )

    val model = VenueDetails(
      "testVenue",
      Contact(null, null),
      Location(null, 1.0, 2.0, null, null, null, null, null),
      null,
      4.5,
      Stats(null, null, null, null),
      null,
    )

    runBlocking {
      val venueId = "test-id"
      `when`(apiServiceMock.fetchVenueDetails(venueId)).thenReturn(
        Response.success(response)
      )

      val result = repository.getVenueDetails(venueId)

      Assert.assertEquals(ResultWrapper.Success(model), result)
    }
  }

  @Test
  fun getVenueDetails_error() {
    runBlocking {
      val venueId = "test-id"
      `when`(apiServiceMock.fetchVenueDetails(venueId)).thenReturn(
        Response.error(500, "error".toResponseBody("plain/text".toMediaTypeOrNull()))
      )

      val result = repository.getVenueDetails(venueId)
      Assert.assertEquals(ResultWrapper.GenericError(), result)
    }
  }

  @Test
  fun getVenueFromCache_success() {
    val venue = Venue(
      "venueId",
      "Venue Name",
      Location(null, 1.0, 2.0, null, null, null, null, null),
    )
    `when`(lruCache.get("venueId")).thenReturn(venue)

    val result = repository.getVenueFromCache("venueId")

    Assert.assertEquals(venue, result)
  }

  @Test
  fun getVenueFromCache_error() {
    `when`(lruCache.get("venueId")).thenReturn(null)

    val result = repository.getVenueFromCache("venueId")

    Assert.assertEquals(null, result)
  }
}